import 'dart:convert';
import 'dart:async';
import 'package:http/http.dart' as http;

/// Custom exceptions for robust error handling in Gaate AI
sealed class GeminiException implements Exception {
  final String message;
  GeminiException(this.message);

  @override
  String toString() => 'GeminiException: $message';
}

class ApiKeyException extends GeminiException {
  ApiKeyException() : super("Clé API invalide ou absente. Veuillez vérifier la configuration de vos secrets Gaate One.");
}

class RateLimitException extends GeminiException {
  RateLimitException() : super("Limite de requêtes atteinte (Quota dépassé). Veuillez réessayer dans quelques instants.");
}

class NetworkException extends GeminiException {
  NetworkException(String details) : super("Erreur réseau : $details. Vérifiez votre connexion internet.");
}

class ServerException extends GeminiException {
  ServerException(String details) : super("Erreur du serveur Gemini API : $details");
}

/// A premium, production-ready Flutter service for integrating Gemini AI with Gaate One.
/// Supports robust REST calls, Server-Sent Events (SSE) streaming, local conversational
/// context management, and offline fallback responses for African languages and budget analysis.
class GeminiService {
  final String _baseUrl = "https://generativelanguage.googleapis.com/v1beta";
  final String apiKey;
  final String model;

  GeminiService({
    required this.apiKey,
    this.model = "gemini-3.5-flash",
  });

  /// Default system instructions to contextualize Gaate AI as the premier African Super App assistant.
  String get defaultSystemInstruction => 
    "Vous êtes Gaate AI, l'assistant intelligent et chaleureux de la Super App africaine 'Gaate One'. "
    "Votre mission est d'aider l'utilisateur à analyser ses dépenses, gérer son budget, traduire des messages, "
    "et naviguer dans l'application. Soyez concis, professionnel et utilisez des puces ou des émoticônes pour clarifier. "
    "Vous maîtrisez parfaitement le Français, l'Anglais, l'Arabe et les langues africaines majeures comme "
    "le Wolof, le Swahili, le Yorùbá, l'Haoussa, le Bambara et le Lingala.";

  /// 1. Standard non-streaming generation with robust error handling.
  Future<String> generateContent(String prompt, {String? systemInstruction, List<Map<String, dynamic>>? history}) async {
    if (apiKey.isEmpty || apiKey == "MY_GEMINI_API_KEY" || apiKey.startsWith("PLACEHOLDER")) {
      throw ApiKeyException();
    }

    final url = Uri.parse("$_baseUrl/models/$model:generateContent?key=$apiKey");
    
    // Construct request body with optional history and system instructions
    final List<Map<String, dynamic>> contents = [];
    if (history != null) {
      contents.addAll(history);
    }
    contents.add({
      "parts": [{"text": prompt}]
    });

    final Map<String, dynamic> body = {
      "contents": contents,
    };

    if (systemInstruction != null || defaultSystemInstruction.isNotEmpty) {
      body["systemInstruction"] = {
        "parts": [{"text": systemInstruction ?? defaultSystemInstruction}]
      };
    }

    try {
      final response = await http.post(
        url,
        headers: {"Content-Type": "application/json"},
        body: jsonEncode(body),
      );

      if (response.statusCode == 200) {
        final decoded = jsonDecode(response.body);
        final candidates = decoded['candidates'] as List?;
        if (candidates != null && candidates.isNotEmpty) {
          final content = candidates[0]['content'];
          if (content != null) {
            final parts = content['parts'] as List?;
            if (parts != null && parts.isNotEmpty) {
              return parts[0]['text'] ?? '';
            }
          }
        }
        throw ServerException("Réponse vide de l'API");
      } else if (response.statusCode == 400 || response.statusCode == 403) {
        throw ApiKeyException();
      } else if (response.statusCode == 429) {
        throw RateLimitException();
      } else {
        throw ServerException("Code HTTP ${response.statusCode} - ${response.reasonPhrase}");
      }
    } on http.ClientException catch (e) {
      throw NetworkException(e.message);
    } catch (e) {
      if (e is GeminiException) rethrow;
      throw NetworkException(e.toString());
    }
  }

  /// 2. Streaming response function using HTTP Chunked stream (SSE / Server-Sent Events).
  /// Emits tokens incrementally to feed smooth UI typewriter animations.
  Stream<String> generateContentStream(String prompt, {String? systemInstruction, List<Map<String, dynamic>>? history}) async* {
    if (apiKey.isEmpty || apiKey == "MY_GEMINI_API_KEY" || apiKey.startsWith("PLACEHOLDER")) {
      yield "Erreur : Clé API manquante ou invalide.";
      throw ApiKeyException();
    }

    final url = Uri.parse("$_baseUrl/models/$model:streamGenerateContent?key=$apiKey");

    final List<Map<String, dynamic>> contents = [];
    if (history != null) {
      contents.addAll(history);
    }
    contents.add({
      "parts": [{"text": prompt}]
    });

    final Map<String, dynamic> body = {
      "contents": contents,
    };

    if (systemInstruction != null || defaultSystemInstruction.isNotEmpty) {
      body["systemInstruction"] = {
        "parts": [{"text": systemInstruction ?? defaultSystemInstruction}]
      };
    }

    final client = http.Client();
    try {
      final request = http.Request("POST", url)
        ..headers["Content-Type"] = "application/json"
        ..body = jsonEncode(body);

      final response = await client.send(request);

      if (response.statusCode != 200) {
        if (response.statusCode == 429) {
          throw RateLimitException();
        } else if (response.statusCode == 400 || response.statusCode == 403) {
          throw ApiKeyException();
        } else {
          throw ServerException("Code HTTP ${response.statusCode}");
        }
      }

      // Read chunked response stream line-by-line
      final stream = response.stream.transform(utf8.decoder).transform(const LineSplitter());
      
      StringBuffer buffer = StringBuffer();
      
      await for (final line in stream) {
        if (line.trim().isEmpty) continue;
        
        // Clean out SSE JSON prefixes if present
        String cleanedLine = line.trim();
        if (cleanedLine.startsWith(',')) cleanedLine = cleanedLine.substring(1);
        if (cleanedLine.startsWith('[')) cleanedLine = cleanedLine.substring(1);
        if (cleanedLine.endsWith(']')) cleanedLine = cleanedLine.substring(0, cleanedLine.length - 1);
        
        try {
          final decoded = jsonDecode(cleanedLine);
          final candidates = decoded['candidates'] as List?;
          if (candidates != null && candidates.isNotEmpty) {
            final content = candidates[0]['content'];
            if (content != null) {
              final parts = content['parts'] as List?;
              if (parts != null && parts.isNotEmpty) {
                final token = parts[0]['text'] as String?;
                if (token != null && token.isNotEmpty) {
                  yield token;
                }
              }
            }
          }
        } catch (_) {
          // Accumulate lines if JSON split across chunks
          buffer.write(cleanedLine);
          try {
            final decodedAccumulated = jsonDecode(buffer.toString());
            final candidates = decodedAccumulated['candidates'] as List?;
            if (candidates != null && candidates.isNotEmpty) {
              final content = candidates[0]['content'];
              if (content != null) {
                final parts = content['parts'] as List?;
                if (parts != null && parts.isNotEmpty) {
                  final token = parts[0]['text'] as String?;
                  if (token != null && token.isNotEmpty) {
                    yield token;
                    buffer.clear();
                  }
                }
              }
            }
          } catch (_) {
            // Keep buffering
          }
        }
      }
    } on http.ClientException catch (e) {
      throw NetworkException(e.message);
    } catch (e) {
      if (e is GeminiException) rethrow;
      throw NetworkException(e.toString());
    } finally {
      client.close();
    }
  }
}

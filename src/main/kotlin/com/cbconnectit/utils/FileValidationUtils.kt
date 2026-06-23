package com.cbconnectit.utils

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/**
 * Utility object for validating file uploads, particularly for security-sensitive operations
 * like serving uploaded files via static routes.
 */
object FileValidationUtils {

    private const val WEBP_MIN_SIZE = 12
    private const val RIFF_SIGNATURE_START = 0
    private const val RIFF_SIGNATURE_END = 3
    private const val WEBP_SIGNATURE_START = 8
    private const val WEBP_SIGNATURE_END = 11

    /**
     * Allowed image MIME types for upload.
     * This allowlist prevents malicious file types (HTML, JS, etc.) from being uploaded
     * and potentially served to clients, which could lead to XSS attacks.
     */
    private val ALLOWED_IMAGE_MIME_TYPES = setOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp",
        "image/svg+xml"
    )

    /**
     * Allowed image file extensions (without the dot).
     */
    private val ALLOWED_IMAGE_EXTENSIONS = setOf(
        "jpg",
        "jpeg",
        "png",
        "gif",
        "webp",
        "svg"
    )

    /**
     * Potentially dangerous SVG patterns that could lead to XSS attacks.
     * These patterns are checked case-insensitively in SVG content.
     */
    private val DANGEROUS_SVG_PATTERNS = listOf(
        // Script tags and event handlers
        "<script",
        "javascript:",
        "onload=",
        "onerror=",
        "onclick=",
        "onmouseover=",
        "onfocus=",
        "onblur=",
        // External references that could leak data or load malicious content
        "<foreignobject", // Can embed HTML/JavaScript
        "xlink:href=\"data:", // Data URIs can contain scripts
        "xlink:href=\"javascript:",
        // Base64 encoded scripts (common obfuscation technique)
        "data:text/html",
        "data:application/javascript",
        // Other potentially dangerous elements
        "<iframe",
        "<embed",
        "<object"
    )

    /**
     * Validates that a file is a legitimate image by checking:
     * 1. Content-Type is in the allowed list
     * 2. File extension is in the allowed list
     * 3. File bytes can be decoded as an actual image (prevents content-type spoofing)
     *
     * Note: WebP and SVG are validated differently since ImageIO doesn't support them by default.
     * SVG files undergo additional security validation to detect potential XSS vectors.
     *
     * @param contentType The MIME type from the upload
     * @param fileName The original filename
     * @param fileBytes The actual file bytes
     * @return true if the file passes all validation checks
     */
    fun isValidImageFile(contentType: String?, fileName: String?, fileBytes: ByteArray): Boolean {
        // Check content type
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.lowercase())) {
            return false
        }

        // Check file extension
        val extension = fileName?.substringAfterLast('.', "")?.lowercase()
        if (extension.isNullOrEmpty() || !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return false
        }

        // SVG files require special validation (XML-based, not supported by ImageIO)
        // We perform basic security checks to prevent stored XSS attacks
        if (contentType.lowercase() == "image/svg+xml") {
            return isValidSVG(fileBytes)
        }

        // WebP files cannot be validated with ImageIO by default, so we check magic bytes
        if (contentType.lowercase() == "image/webp") {
            return isValidWebP(fileBytes)
        }

        // Validate that the file bytes can actually be decoded as an image
        // This prevents uploading non-image files with spoofed content-type/extension
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        return try {
            val image = ImageIO.read(ByteArrayInputStream(fileBytes))
            image != null
        } catch (_: Exception) {
            // Intentionally catching all exceptions for security:
            // Any error during image decoding means it's not a valid image
            false
        }
    }

    /**
     * Validates SVG file format by checking for potentially dangerous patterns.
     *
     * SVG files are XML-based and can contain JavaScript, event handlers, and external references
     * that could lead to XSS attacks when served from the same origin as the application.
     *
     * This validation provides a basic layer of defense by detecting common XSS vectors.
     *
     * **Note for Production/CDN deployment:**
     * - Serve SVG files from a separate domain (e.g., cdn.example.com vs api.example.com)
     * - Set Content-Type: image/svg+xml (never text/html)
     * - Add Content-Security-Policy header: default-src 'none'; style-src 'unsafe-inline'
     * - Add X-Content-Type-Options: nosniff
     * - Consider Content-Disposition: attachment for user-uploaded SVGs
     *
     * @param fileBytes The SVG file bytes to validate
     * @return true if the file appears safe, false if dangerous patterns are detected
     */
    private fun isValidSVG(fileBytes: ByteArray): Boolean {
        @Suppress("TooGenericExceptionCaught")
        return try {
            // Convert bytes to string (SVG is text-based XML)
            val svgContent = String(fileBytes, Charsets.UTF_8).lowercase()

            // Basic XML structure check - must contain <svg
            if (!svgContent.contains("<svg")) {
                return false
            }

            // Check for dangerous patterns
            for (pattern in DANGEROUS_SVG_PATTERNS) {
                if (svgContent.contains(pattern.lowercase())) {
                    return false
                }
            }

            true
        } catch (_: Exception) {
            // Any error in parsing means it's not a valid SVG
            false
        }
    }

    /**
     * Validates WebP file format by checking magic bytes.
     * WebP files start with "RIFF" and have "WEBP" at offset 8.
     *
     * @param fileBytes The file bytes to validate
     * @return true if the file has valid WebP signature
     */
    private fun isValidWebP(fileBytes: ByteArray): Boolean {
        // WebP files must be at least 12 bytes (RIFF + size + WEBP)
        if (fileBytes.size < WEBP_MIN_SIZE) return false

        // Check for "RIFF" at the start (bytes 0-3)
        val riffSignature = String(fileBytes.sliceArray(RIFF_SIGNATURE_START..RIFF_SIGNATURE_END), Charsets.US_ASCII)
        if (riffSignature != "RIFF") return false

        // Check for "WEBP" at offset 8 (bytes 8-11)
        val webpSignature = String(fileBytes.sliceArray(WEBP_SIGNATURE_START..WEBP_SIGNATURE_END), Charsets.US_ASCII)
        return webpSignature == "WEBP"
    }

    /**
     * Gets a descriptive error message for why a file was rejected.
     * Useful for debugging and logging (but should not be exposed to end users for security).
     */
    fun getValidationErrorMessage(contentType: String?, fileName: String?, fileBytes: ByteArray): String {
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.lowercase())) {
            return "Invalid content type: $contentType. Allowed types: ${ALLOWED_IMAGE_MIME_TYPES.joinToString()}"
        }

        val extension = fileName?.substringAfterLast('.', "")?.lowercase()
        if (extension.isNullOrEmpty() || !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return "Invalid file extension: $extension. Allowed extensions: ${ALLOWED_IMAGE_EXTENSIONS.joinToString()}"
        }

        if (contentType.lowercase() == "image/svg+xml") {
            return if (isValidSVG(fileBytes)) {
                "SVG validation passed"
            } else {
                "Invalid SVG file: contains potentially dangerous content (scripts, event handlers, or external references)"
            }
        }

        if (contentType.lowercase() == "image/webp") {
            return if (isValidWebP(fileBytes)) {
                "WebP validation passed"
            } else {
                "Invalid WebP file: magic bytes check failed"
            }
        }

        @Suppress("TooGenericExceptionCaught")
        return try {
            val image = ImageIO.read(ByteArrayInputStream(fileBytes))
            if (image == null) {
                "File bytes could not be decoded as a valid image"
            } else {
                "Image validation passed"
            }
        } catch (e: Exception) {
            "Error decoding image: ${e.message}"
        }
    }
}

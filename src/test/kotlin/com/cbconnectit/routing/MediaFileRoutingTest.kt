package com.cbconnectit.routing

import com.cbconnectit.data.dto.responses.mediafile.MediaFileDto
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.instrumentation.MediaFileInstrumentation
import com.cbconnectit.modules.mediafile.MediaFileController
import com.cbconnectit.modules.mediafile.mediaFileRouting
import com.cbconnectit.plugins.statuspages.ErrorInvalidUUID
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorResponse
import com.cbconnectit.plugins.statuspages.toErrorResponse
import com.cbconnectit.utils.ParamConstants
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MediaFileRoutingTest : BaseRoutingTest() {

    private val mediaFileController: MediaFileController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { mediaFileController }
        }
        moduleList = {
            routing { mediaFileRouting(json, mediaFileController) }
        }
    }

    @BeforeEach
    fun clearMocks() {
        io.mockk.clearMocks(mediaFileController)
    }

    private fun withBaseMediaFileTestApplication(
        role: User.Role = User.Role.Admin,
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = withBaseTestApplication(
        AuthenticationInstrumentation(ParamConstants.ADMIN_AUTHENTICATE_KEY, role)
    ) { block() }

    // <editor-fold desc="Create (POST)">
    // Note: Full multipart/form-data testing is complex in unit tests.
    // These tests focus on authentication and authorization checks.
    // The actual multipart parsing and file handling is tested at the controller level.

    @Test
    fun `when creating media file but not admin, we return 403 error`() = withBaseMediaFileTestApplication(User.Role.User) {
        val response = doCall(HttpMethod.Post, "/media-files", multipartCall = true, authorized = true)
        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when creating media file but not authorized, we return 401 error`() = withBaseMediaFileTestApplication {
        val response = doCall(HttpMethod.Post, "/media-files", multipartCall = true, authorized = false)
        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    // Full multipart testing would require complex setup with actual file uploads.
    // The MediaFileController tests cover the business logic thoroughly.
    // Integration tests would be needed for end-to-end multipart upload testing.
    // </editor-fold>

    // <editor-fold desc="Read All">
    @Test
    fun `when fetching all media files, we return a list`() = withBaseMediaFileTestApplication {
        val mediaFileDto = MediaFileDto(
            id = "00000000-0000-0000-0000-000000000001",
            url = "https://example.com/test.jpg",
            ownerId = "00000000-0000-0000-0000-000000000010",
            ownerType = OwnerType.TESTIMONIAL,
            mediaType = MediaType.IMAGE,
            fileSize = 1024L,
            originalFilename = "test.jpg",
            altText = "Test image",
            mimeType = "image/jpeg",
            width = 800,
            height = 600,
            createdAt = "2024-01-01 00:00:00",
            updatedAt = "2024-01-01 00:00:00"
        )

        coEvery { mediaFileController.readAll() } returns listOf(mediaFileDto)

        val response = doCall(HttpMethod.Get, "/media-files")

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.parseBody<List<MediaFileDto>>()).hasSize(1)
    }

    @Test
    fun `when fetching all media files but not admin, we return 403 error`() = withBaseMediaFileTestApplication(User.Role.User) {
        val response = doCall(HttpMethod.Get, "/media-files")

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `when fetching all media files but not authorized, we return 401 error`() = withBaseMediaFileTestApplication {
        val response = doCall(HttpMethod.Get, "/media-files", body = null, authorized = false)

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }
    // </editor-fold>

    // <editor-fold desc="Read By Id">
    @Test
    fun `when fetching a specific media file with invalid UUID, we return ErrorInvalidUUID`() = withBaseMediaFileTestApplication {
        val response = doCall(HttpMethod.Get, "/media-files/invalid-uuid")

        Assertions.assertThat(response.status).isEqualTo(ErrorInvalidUUID.statusCode)
        Assertions.assertThat(response.parseBody<ErrorResponse>()).isEqualTo(ErrorInvalidUUID.toErrorResponse())
    }

    @Test
    fun `when fetching a specific media file that does not exist, we return error`() = withBaseMediaFileTestApplication {
        coEvery { mediaFileController.readById(any()) } throws ErrorNotFound

        val response = doCall(HttpMethod.Get, "/media-files/00000000-0000-0000-0000-000000000001")

        Assertions.assertThat(response.status).isEqualTo(ErrorNotFound.statusCode)
        Assertions.assertThat(response.parseBody<ErrorResponse>()).isEqualTo(ErrorNotFound.toErrorResponse())
    }

    @Test
    fun `when fetching a specific media file that exists, we return that media file`() = withBaseMediaFileTestApplication {
        val mediaFileDto = MediaFileDto(
            id = "00000000-0000-0000-0000-000000000001",
            url = "https://example.com/test.jpg",
            ownerId = "00000000-0000-0000-0000-000000000010",
            ownerType = OwnerType.TESTIMONIAL,
            mediaType = MediaType.IMAGE,
            fileSize = 1024L,
            originalFilename = "test.jpg",
            altText = "Test image",
            mimeType = "image/jpeg",
            width = 800,
            height = 600,
            createdAt = "2024-01-01 00:00:00",
            updatedAt = "2024-01-01 00:00:00"
        )

        coEvery { mediaFileController.readById(any()) } returns mediaFileDto

        val response = doCall(HttpMethod.Get, "/media-files/00000000-0000-0000-0000-000000000001")

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.parseBody<MediaFileDto>().id).isEqualTo(mediaFileDto.id)
    }

    @Test
    fun `when fetching a specific media file but not admin, we return 403 error`() = withBaseMediaFileTestApplication(User.Role.User) {
        val response = doCall(HttpMethod.Get, "/media-files/00000000-0000-0000-0000-000000000001")

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }
    // </editor-fold>

    // <editor-fold desc="Update">
    @Test
    fun `when updating media file with valid data, we return updated media file`() = withBaseMediaFileTestApplication {
        val updateRequest = MediaFileInstrumentation.givenAValidUpdateMediaFile()
        val mediaFileDto = MediaFileDto(
            id = "00000000-0000-0000-0000-000000000001",
            url = "https://example.com/test.jpg",
            ownerId = "00000000-0000-0000-0000-000000000010",
            ownerType = OwnerType.TESTIMONIAL,
            mediaType = MediaType.IMAGE,
            fileSize = 1024L,
            originalFilename = "test.jpg",
            altText = "Updated alt text",
            mimeType = "image/jpeg",
            width = 800,
            height = 600,
            createdAt = "2024-01-01 00:00:00",
            updatedAt = "2024-01-01 00:00:00"
        )

        coEvery { mediaFileController.update(any(), any()) } returns mediaFileDto

        val body = toJsonBody(updateRequest)
        val response = doCall(HttpMethod.Put, "/media-files/00000000-0000-0000-0000-000000000001", body)

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.parseBody<MediaFileDto>().altText).isEqualTo("Updated alt text")
    }

    @Test
    fun `when updating media file with invalid UUID, we return ErrorInvalidUUID`() = withBaseMediaFileTestApplication {
        val updateRequest = MediaFileInstrumentation.givenAValidUpdateMediaFile()
        val body = toJsonBody(updateRequest)

        val response = doCall(HttpMethod.Put, "/media-files/invalid-uuid", body)

        Assertions.assertThat(response.status).isEqualTo(ErrorInvalidUUID.statusCode)
        Assertions.assertThat(response.parseBody<ErrorResponse>()).isEqualTo(ErrorInvalidUUID.toErrorResponse())
    }

    @Test
    fun `when updating media file that does not exist, we return ErrorNotFound`() = withBaseMediaFileTestApplication {
        val updateRequest = MediaFileInstrumentation.givenAValidUpdateMediaFile()

        coEvery { mediaFileController.update(any(), any()) } throws ErrorNotFound

        val body = toJsonBody(updateRequest)
        val response = doCall(HttpMethod.Put, "/media-files/00000000-0000-0000-0000-000000000001", body)

        Assertions.assertThat(response.status).isEqualTo(ErrorNotFound.statusCode)
    }

    @Test
    fun `when updating media file but not admin, we return 403 error`() = withBaseMediaFileTestApplication(User.Role.User) {
        val updateRequest = MediaFileInstrumentation.givenAValidUpdateMediaFile()
        val body = toJsonBody(updateRequest)

        val response = doCall(HttpMethod.Put, "/media-files/00000000-0000-0000-0000-000000000001", body)

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }
    // </editor-fold>

    // <editor-fold desc="Delete">
    @Test
    fun `when deleting media file with valid id, we return NoContent`() = withBaseMediaFileTestApplication {
        coEvery { mediaFileController.delete(any()) } returns true

        val response = doCall(HttpMethod.Delete, "/media-files/00000000-0000-0000-0000-000000000001")

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
    }

    @Test
    fun `when deleting media file with invalid UUID, we return ErrorInvalidUUID`() = withBaseMediaFileTestApplication {
        val response = doCall(HttpMethod.Delete, "/media-files/invalid-uuid")

        Assertions.assertThat(response.status).isEqualTo(ErrorInvalidUUID.statusCode)
        Assertions.assertThat(response.parseBody<ErrorResponse>()).isEqualTo(ErrorInvalidUUID.toErrorResponse())
    }

    @Test
    fun `when deleting media file that does not exist, we return ErrorNotFound`() = withBaseMediaFileTestApplication {
        coEvery { mediaFileController.delete(any()) } throws ErrorNotFound

        val response = doCall(HttpMethod.Delete, "/media-files/00000000-0000-0000-0000-000000000001")

        Assertions.assertThat(response.status).isEqualTo(ErrorNotFound.statusCode)
    }

    @Test
    fun `when deleting media file but not admin, we return 403 error`() = withBaseMediaFileTestApplication(User.Role.User) {
        val response = doCall(HttpMethod.Delete, "/media-files/00000000-0000-0000-0000-000000000001")

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }
    // </editor-fold>
}

package com.cbconnectit.instrumentation

import com.cbconnectit.data.dto.requests.mediafile.InsertMediaFile
import com.cbconnectit.data.dto.requests.mediafile.UpdateMediaFile
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import java.time.LocalDateTime
import java.util.*

object MediaFileInstrumentation {
    fun givenAValidInsertMediaFile(
        ownerId: String = "00000000-0000-0000-0000-000000000001",
        ownerType: OwnerType = OwnerType.TESTIMONIAL
    ) = InsertMediaFile(
        ownerId = ownerId,
        ownerType = ownerType,
        mediaType = MediaType.IMAGE,
        altText = "Test image"
    )

    fun givenAValidUpdateMediaFile() = UpdateMediaFile(
        altText = "Updated alt text"
    )

    fun givenAnEmptyUpdateMediaFile() = UpdateMediaFile()

    fun givenAMediaFile(
        id: String = "00000000-0000-0000-0000-000000000001",
        ownerId: String = "00000000-0000-0000-0000-000000000002",
        ownerType: OwnerType = OwnerType.TESTIMONIAL
    ): MediaFile {
        val time = LocalDateTime.now()
        return MediaFile(
            id = UUID.fromString(id),
            url = "https://example.com/images/test.jpg",
            ownerId = UUID.fromString(ownerId),
            ownerType = ownerType,
            mediaType = MediaType.IMAGE,
            fileSize = 1024L,
            originalFilename = "test.jpg",
            altText = "Test image",
            mimeType = "image/jpeg",
            width = 800,
            height = 600,
            createdAt = time,
            updatedAt = time
        )
    }

    fun givenMediaFileList() = listOf(
        givenAMediaFile("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000010", OwnerType.TESTIMONIAL),
        givenAMediaFile("00000000-0000-0000-0000-000000000002", "00000000-0000-0000-0000-000000000011", OwnerType.PROJECT),
        givenAMediaFile("00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000012", OwnerType.SERVICE),
        givenAMediaFile("00000000-0000-0000-0000-000000000004", "00000000-0000-0000-0000-000000000013", OwnerType.TESTIMONIAL)
    )
}

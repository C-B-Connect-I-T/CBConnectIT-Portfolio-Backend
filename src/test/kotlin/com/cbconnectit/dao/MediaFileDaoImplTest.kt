package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.MediaFileDaoImpl
import com.cbconnectit.data.database.tables.MediaFilesTable
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertNull

internal class MediaFileDaoImplTest : BaseDaoTest() {

    private val dao = MediaFileDaoImpl()

    private val serviceOwnerId = UUID.fromString("00000000-0000-0000-0000-000000000010")
    private val imageMediaFileId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val bannerMediaFileId = UUID.fromString("00000000-0000-0000-0000-000000000002")

    override suspend fun seedData() {
        MediaFilesTable.insert {
            it[id] = imageMediaFileId
            it[url] = "https://example.com/image.jpg"
            it[ownerId] = serviceOwnerId
            it[ownerType] = OwnerType.SERVICE
            it[mediaType] = MediaType.IMAGE
            it[fileSize] = 1024L
            it[originalFilename] = "image.jpg"
            it[altText] = "Service image"
            it[mimeType] = "image/jpeg"
        }

        MediaFilesTable.insert {
            it[id] = bannerMediaFileId
            it[url] = "https://example.com/banner.jpg"
            it[ownerId] = serviceOwnerId
            it[ownerType] = OwnerType.SERVICE
            it[mediaType] = MediaType.BANNER
            it[fileSize] = 2048L
            it[originalFilename] = "banner.jpg"
            it[altText] = "Service banner"
            it[mimeType] = "image/jpeg"
        }
    }

    // <editor-fold desc="readByOwnerIdAndMediaType">
    @Test
    fun `readByOwnerIdAndMediaType returns IMAGE media file for SERVICE owner`() = runTest {
        val result = dao.readByOwnerIdAndMediaType(serviceOwnerId, OwnerType.SERVICE, MediaType.IMAGE)

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(imageMediaFileId)
        assertThat(result.mediaType).isEqualTo(MediaType.IMAGE)
        assertThat(result.altText).isEqualTo("Service image")
    }

    @Test
    fun `readByOwnerIdAndMediaType returns BANNER media file for SERVICE owner`() = runTest {
        val result = dao.readByOwnerIdAndMediaType(serviceOwnerId, OwnerType.SERVICE, MediaType.BANNER)

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(bannerMediaFileId)
        assertThat(result.mediaType).isEqualTo(MediaType.BANNER)
        assertThat(result.altText).isEqualTo("Service banner")
    }

    @Test
    fun `readByOwnerIdAndMediaType returns null when no media file exists for owner and type`() = runTest {
        val result = dao.readByOwnerIdAndMediaType(UUID.randomUUID(), OwnerType.SERVICE, MediaType.IMAGE)

        assertNull(result)
    }

    @Test
    fun `IMAGE and BANNER can coexist for the same SERVICE owner`() = runTest {
        val image = dao.readByOwnerIdAndMediaType(serviceOwnerId, OwnerType.SERVICE, MediaType.IMAGE)
        val banner = dao.readByOwnerIdAndMediaType(serviceOwnerId, OwnerType.SERVICE, MediaType.BANNER)

        assertThat(image).isNotNull
        assertThat(banner).isNotNull
        assertThat(image!!.id).isNotEqualTo(banner!!.id)
    }
    // </editor-fold>
}

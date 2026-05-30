package com.cbconnectit.data.dto.requests.project

import com.cbconnectit.utils.isValidUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsertNewProject(
    @SerialName("banner_image_url")
    val bannerImageUrl: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val title: String,
    @SerialName("short_description")
    val shortDescription: String,
    val description: String,
    val tags: List<String>? = null,
    val links: List<String>? = null
) {
    val isValid
        get() = title.isNotBlank() &&
                shortDescription.isNotBlank() &&
                description.isNotBlank() &&
                (bannerImageUrl == null || bannerImageUrl.isValidUrl) &&
                (imageUrl == null || imageUrl.isValidUrl)
}

@Serializable
data class UpdateProject(
    @SerialName("banner_image_url")
    val bannerImageUrl: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val title: String,
    @SerialName("short_description")
    val shortDescription: String,
    val description: String,
    val tags: List<String>? = null,
    val links: List<String>? = null
) {
    val isValid
        get() = title.isNotBlank() &&
                shortDescription.isNotBlank() &&
                description.isNotBlank() &&
                (bannerImageUrl == null || bannerImageUrl.isValidUrl) &&
                (imageUrl == null || imageUrl.isValidUrl)
}

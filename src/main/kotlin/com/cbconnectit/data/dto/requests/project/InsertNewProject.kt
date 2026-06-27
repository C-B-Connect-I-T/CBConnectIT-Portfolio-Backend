package com.cbconnectit.data.dto.requests.project

import com.cbconnectit.utils.isValidUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsertNewProject(
    val title: String,
    @SerialName("short_description")
    val shortDescription: String,
    val description: String,
    @SerialName("image_alt_text")
    val imageAltText: String? = null,
    @SerialName("banner_image_alt_text")
    val bannerImageAltText: String? = null,
    val tags: List<String>? = null,
    val links: List<String>? = null
) {
    val isValid
        get() = title.isNotBlank() &&
                shortDescription.isNotBlank() &&
                description.isNotBlank() &&
                (links == null || links.all { it.isNotBlank() && it.isValidUrl })
}

@Serializable
data class UpdateProject(
    val title: String,
    @SerialName("short_description")
    val shortDescription: String,
    val description: String,
    @SerialName("image_alt_text")
    val imageAltText: String? = null,
    @SerialName("banner_image_alt_text")
    val bannerImageAltText: String? = null,
    val tags: List<String>? = null,
    val links: List<String>? = null
) {
    val isValid
        get() = title.isNotBlank() &&
                shortDescription.isNotBlank() &&
                description.isNotBlank() &&
                (links == null || links.all { it.isNotBlank() && it.isValidUrl })
}

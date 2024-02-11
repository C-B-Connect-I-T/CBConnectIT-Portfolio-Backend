package com.cbconnectit.data.dto.requests.project

import com.cbconnectit.utils.isValidUrl
import com.google.gson.annotations.SerializedName

data class InsertNewProject(
    @SerializedName("banner_image")
    val bannerImage: String? = null,
    val image: String? = null,
    val title: String,
    @SerializedName("short_description")
    val shortDescription: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val links: List<String> = emptyList()
) {
    val isValid
        get() = title.isNotBlank() &&
                shortDescription.isNotBlank() &&
                description.isNotBlank() &&
                (bannerImage == null || bannerImage.isValidUrl) &&
                (image == null || image.isValidUrl) &&
                tags.isNotEmpty() &&
                links.isNotEmpty()
}

data class UpdateProject(
    @SerializedName("banner_image")
    val bannerImage: String? = null,
    val image: String? = null,
    val title: String,
    @SerializedName("short_description")
    val shortDescription: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val links: List<String> = emptyList()
) {
    val isValid
        get() = title.isNotBlank() &&
                shortDescription.isNotBlank() &&
                description.isNotBlank() &&
                (bannerImage == null || bannerImage.isValidUrl) &&
                (image == null || image.isValidUrl) &&
                tags.isNotEmpty() &&
                links.isNotEmpty()
}

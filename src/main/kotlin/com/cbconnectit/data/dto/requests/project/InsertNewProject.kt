package com.cbconnectit.data.dto.requests.project

import com.cbconnectit.utils.isValidUrl
import com.google.gson.annotations.SerializedName

data class InsertNewProject(
    @SerializedName("banner_image_url")
    val bannerImageUrl: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    val title: String,
    @SerializedName("short_description")
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

data class UpdateProject(
    @SerializedName("banner_image_url")
    val bannerImageUrl: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    val title: String,
    @SerializedName("short_description")
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

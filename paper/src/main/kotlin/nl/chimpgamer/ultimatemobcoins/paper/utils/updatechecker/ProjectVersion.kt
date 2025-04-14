package nl.chimpgamer.ultimatemobcoins.paper.utils.updatechecker

import com.google.gson.annotations.SerializedName

data class ProjectVersion(
    @SerializedName("game_versions")
    val gameVersions: List<String>,
    val loaders: List<String>,
    val id: String,
    @SerializedName("project_id")
    val projectId: String,
    @SerializedName("authorId")
    val authorId: String,
    val featured: Boolean,
    val name: String,
    @SerializedName("version_number")
    val versionNumber: String,
    val changelog: String,
    @SerializedName("changelogUrl")
    val changelogUrl: String?,
    @SerializedName("date_published")
    val datePublished: String,
    val downloads: Int,
    @SerializedName("version_type")
    val versionType: String,
    val status: String,
    @SerializedName("requested_status")
    val requestedStatus: String?,
)
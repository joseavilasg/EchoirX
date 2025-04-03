package app.echoirx.domain.model

import androidx.annotation.StringRes
import app.echoirx.R

enum class AlbumFolderFormat(
    @StringRes val displayNameResId: Int,
    val previewText: String,
    val format: (String, String) -> String
) {
    ALBUM_ONLY(
        R.string.folder_format_album_only_display,
        "Album",
        { _, album -> album }
    ),
    ARTIST_ONLY(
        R.string.folder_format_artist_only_display,
        "Artist",
        { artist, _ -> artist.split(",").first().trim() }
    ),
    ARTIST_ALBUM(
        R.string.folder_format_artist_album_display,
        "Artist/Album",
        { artist, album -> "${artist.split(",").first().trim()}/$album" }
    );

    companion object {
        fun fromOrdinal(ordinal: Int) = entries.getOrNull(ordinal) ?: ALBUM_ONLY
    }
}
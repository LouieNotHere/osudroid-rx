package com.reco1l.osu.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import java.io.Serial
import java.io.Serializable

@Entity
data class BeatmapOptions(

    /**
     * The beatmap set directory.
     */
    @PrimaryKey
    val setDirectory: String,

    /**
     * Whether the beatmap is marked as favorite.
     */
    var isFavorite: Boolean = false,

    /**
     * The beatmap offset.
     */
    var offset: Int = 0

)

@Dao interface IBeatmapOptionsDAO {

    @Query("SELECT * FROM BeatmapOptions WHERE setDirectory = :setDirectory")
    fun getOptions(setDirectory: String): BeatmapOptions?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(options: BeatmapOptions)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(options: BeatmapOptions)

    @Query("DELETE FROM BeatmapOptions")
    fun deleteAll()

}


@Deprecated("This class is used to do legacy migration, should not be used and will be removed in future releases.")
class BeatmapProperties : Serializable {

    var offset = 0

    var favorite = false

    companion object {
        @Serial
        private const val serialVersionUID = -7229486402310659139L
    }
}


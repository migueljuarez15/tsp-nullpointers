import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PARADA")
data class ParadaModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID_PARADA")
    val id: Int = 0,

    @ColumnInfo(name = "NOMBRE")
    val nombre: String,

    @ColumnInfo(name = "LATITUD")
    val color: Double,

    @ColumnInfo(name = "LONGITUD")
    val horario: Double,
)
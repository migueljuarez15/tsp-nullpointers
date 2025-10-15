import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "RUTA")
data class RutaModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID_RUTA")
    val id: Int = 0,

    @ColumnInfo(name = "NOMBRE")
    val nombre: String,

    @ColumnInfo(name = "COLOR")
    val color: String,

    @ColumnInfo(name = "HORARIO")
    val horario: String?,

    @ColumnInfo(name = "TIEMPO_ESTIMADO_ESPERA")
    val tiempoEstimadoEspera: String?
)
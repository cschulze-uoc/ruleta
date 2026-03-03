package com.apktados.ruleta.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.apktados.ruleta.game.TipoApuesta

@Composable
fun ApuestaCheckbox(
    label: String,
    tipo: TipoApuesta,
    apuestasSeleccionadas: Set<TipoApuesta>,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = tipo in apuestasSeleccionadas,
            onCheckedChange = onCheckedChange
        )
        Text(label,color = androidx.compose.ui.graphics.Color.White)
    }
}

fun toggleApuesta(
    current: Set<TipoApuesta>,
    tipo: TipoApuesta,
    checked: Boolean
): Set<TipoApuesta> {

    val opuesto = when (tipo) {
        TipoApuesta.ROJO -> TipoApuesta.NEGRO
        TipoApuesta.NEGRO -> TipoApuesta.ROJO
        TipoApuesta.PAR -> TipoApuesta.IMPAR
        TipoApuesta.IMPAR -> TipoApuesta.PAR
        TipoApuesta.PASSE -> TipoApuesta.MANQUE
        TipoApuesta.MANQUE -> TipoApuesta.PASSE
    }

    return if (checked) {
        // Añadir tipo y quitar su opuesto
        (current - opuesto) + tipo
    } else {
        // Quitar tipo
        current - tipo
    }
}
package com.marcos.fisikappmovil.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.model.TipoAviso; // Importamos tu Enum real


public class Notificacion {

    public static void mostrarAviso(Context context, TipoAviso tipo, String titulo, String mensaje) {

        // 1. Inflamos tu diseño XML real (layout_notificacion)
        LayoutInflater inflater = LayoutInflater.from(context);
        View vista = inflater.inflate(R.layout.layout_notificacion, null);

        // 2. Vinculamos los componentes
        LinearLayout contenedor = vista.findViewById(R.id.contenedorNotificacion);
        ImageView imgIcono = vista.findViewById(R.id.imgIconoAviso);
        TextView txtTitulo = vista.findViewById(R.id.txtTituloAviso);
        TextView txtMensaje = vista.findViewById(R.id.txtMensajeAviso);

        // 3. Asignamos los textos
        txtTitulo.setText(titulo);
        txtMensaje.setText(mensaje);

        int colorFondoRes;
        int iconoRes;

        // 4. Evaluamos el tipo de aviso
        switch (tipo) {
            case EXITO:
                colorFondoRes = R.color.colorExito;
                iconoRes = R.drawable.ic_check_circle;
                break;
            case ERROR:
                colorFondoRes = R.color.colorError;
                iconoRes = R.drawable.ic_error;
                break;
            case ADVERTENCIA:
                colorFondoRes = R.color.colorAdvertencia;
                iconoRes = R.drawable.ic_warning;
                break;
            case INFO:
            default:
                colorFondoRes = R.color.colorInfo;
                iconoRes = R.drawable.ic_info;
                break;
        }

        // 5. Aplicamos el color de fondo al XML
        Drawable fondoDrawable = contenedor.getBackground();
        if (fondoDrawable != null) {
            fondoDrawable = DrawableCompat.wrap(fondoDrawable);
            DrawableCompat.setTint(fondoDrawable, ContextCompat.getColor(context, colorFondoRes));
            contenedor.setBackground(fondoDrawable);
        }

        // 6. Asignamos el icono
        imgIcono.setImageResource(iconoRes);

        // 7. Mostramos el Toast personalizado
        Toast toast = new Toast(context.getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(vista);
        toast.show();
    }
}
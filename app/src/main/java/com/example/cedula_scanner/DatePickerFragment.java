package com.example.cedula_scanner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private EditText editText;

    public DatePickerFragment() {
        this.editText = editText;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Obtener la fecha actual
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear un nuevo DatePickerDialog y devolverlo
        return new DatePickerDialog(requireActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Cuando el usuario selecciona una fecha en el diálogo del calendario,
        // actualiza el campo de entrada de fecha con la fecha seleccionada
        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
        editText.setText(selectedDate);
    }
}

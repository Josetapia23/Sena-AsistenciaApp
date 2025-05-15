package com.example.cedula_scanner;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SelectorEmpresaDialog extends Dialog {

    private EmpresaManager empresaManager;
    private EditText etBusqueda;
    private ListView listViewEmpresas;
    private Button btnCrearNueva;
    private Button btnCancelar;
    private TextView tvNoResultados;

    private List<EmpresaManager.Empresa> empresasFiltradas = new ArrayList<>();
    private ArrayAdapter<EmpresaManager.Empresa> adapter;

    private OnEmpresaSelectedListener listener;

    // Interfaz para callback cuando se selecciona una empresa
    public interface OnEmpresaSelectedListener {
        void onEmpresaSelected(EmpresaManager.Empresa empresa);
        void onNuevaEmpresa(String nombreEmpresa);
        void onDialogCancelled();
    }

    public SelectorEmpresaDialog(@NonNull Context context, EmpresaManager empresaManager, OnEmpresaSelectedListener listener) {
        super(context);
        this.empresaManager = empresaManager;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_selector_empresa);

        // Configurar para que ocupe la mayor parte de la pantalla
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        // Inicializar vistas
        etBusqueda = findViewById(R.id.etBusquedaEmpresa);
        listViewEmpresas = findViewById(R.id.listViewEmpresas);
        btnCrearNueva = findViewById(R.id.btnCrearNuevaEmpresa);
        btnCancelar = findViewById(R.id.btnCancelarSeleccion);
        tvNoResultados = findViewById(R.id.tvNoResultados);

        // Configurar adaptador para la lista
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, empresasFiltradas);
        listViewEmpresas.setAdapter(adapter);

        // Configurar listeners
        etBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filtrarEmpresas(s.toString());
            }
        });

        listViewEmpresas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < empresasFiltradas.size()) {
                    EmpresaManager.Empresa empresa = empresasFiltradas.get(position);
                    if (listener != null) {
                        listener.onEmpresaSelected(empresa);
                    }
                    dismiss();
                }
            }
        });

        btnCrearNueva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreEmpresa = etBusqueda.getText().toString().trim();
                if (!nombreEmpresa.isEmpty()) {
                    if (listener != null) {
                        listener.onNuevaEmpresa(nombreEmpresa);
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Ingrese el nombre de la empresa", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDialogCancelled();
                }
                dismiss();
            }
        });

        // Cargar todas las empresas inicialmente
        cargarTodasLasEmpresas();
    }

    private void cargarTodasLasEmpresas() {
        if (empresaManager.isDatosCargados()) {
            empresasFiltradas.clear();
            empresasFiltradas.addAll(empresaManager.getListaEmpresas());
            adapter.notifyDataSetChanged();
            actualizarEstadoNoResultados();
        } else {
            // Si los datos no están cargados, configurar un listener para cuando se carguen
            empresaManager.addOnEmpresasLoadedListener(new EmpresaManager.OnEmpresasLoadedListener() {
                @Override
                public void onEmpresasLoaded(List<EmpresaManager.Empresa> empresas) {
                    empresasFiltradas.clear();
                    empresasFiltradas.addAll(empresas);
                    adapter.notifyDataSetChanged();
                    actualizarEstadoNoResultados();
                    // Remover el listener después de utilizarlo
                    empresaManager.removeOnEmpresasLoadedListener(this);
                }
            });
            // Forzar carga de datos
            empresaManager.cargarEmpresasDesdeServidor();
        }
    }

    private void filtrarEmpresas(String query) {
        List<EmpresaManager.Empresa> resultados = empresaManager.buscarEmpresasPorNombre(query);
        empresasFiltradas.clear();
        empresasFiltradas.addAll(resultados);
        adapter.notifyDataSetChanged();
        actualizarEstadoNoResultados();

        // Actualizar el texto del botón para crear nueva empresa
        if (!query.isEmpty()) {
            btnCrearNueva.setText("Crear empresa: " + query);
            btnCrearNueva.setEnabled(true);
        } else {
            btnCrearNueva.setText("Crear nueva empresa");
            btnCrearNueva.setEnabled(false);
        }
    }

    private void actualizarEstadoNoResultados() {
        if (empresasFiltradas.isEmpty()) {
            tvNoResultados.setVisibility(View.VISIBLE);
            listViewEmpresas.setVisibility(View.GONE);
        } else {
            tvNoResultados.setVisibility(View.GONE);
            listViewEmpresas.setVisibility(View.VISIBLE);
        }
    }
}
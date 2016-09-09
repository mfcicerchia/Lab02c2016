package com.pharmacy.martin.lab02c2016;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.nfc.Tag;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,
        RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemClickListener {

    private ListView listaMenu;
    private ToggleButton btnDelivery;
    private RadioGroup rdBtnGrupo;
    private RadioButton rdBtnPlato, rdBtnPostre, rdBtnBebida;
    private Button btnAgregar, btnReiniciar, btnConfirmar;
    private Spinner spnrHorario;
    private Switch swNotificacion;
    private TextView tvDatosPedido;
    private double costo = 0;
    private boolean pedidoConfirmado = false;

    ArrayList<ItemPedido> elementosPedidos = new ArrayList<ItemPedido>();
    ArrayAdapter<ElementoMenu> adaptador;
    ArrayAdapter<String> adaptador2;


    /**
     * Listas De instancia
     */
    private ElementoMenu[] listaBebidas;
    private ElementoMenu[] listaPlatos;
    private ElementoMenu[] listaPostres;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.iniciarListas();

        btnDelivery = (ToggleButton) findViewById(R.id.btnDelivery);
        btnDelivery.setOnCheckedChangeListener(this);

        rdBtnGrupo = (RadioGroup) findViewById(R.id.radGrp);
        rdBtnGrupo.setOnCheckedChangeListener(this);

        listaMenu = (ListView) findViewById(R.id.lvCarta);
        listaMenu.setChoiceMode(listaMenu.CHOICE_MODE_MULTIPLE);
        listaMenu.setOnItemClickListener(this);

        tvDatosPedido = (TextView) findViewById(R.id.tvDatosPedido);
        tvDatosPedido.setMovementMethod(new ScrollingMovementMethod());


        spnrHorario = (Spinner) findViewById(R.id.spnrHorarios);
        final String[] horarios = new String[] {"20:00","20:30","21:00","21:30","22:00","22:30","23:00"};
        ArrayList<String> arrayListHorarios = new ArrayList<String>(Arrays.asList(horarios));
        adaptador2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayListHorarios);
        adaptador2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrHorario.setAdapter(adaptador2);
        spnrHorario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        /**Asignacion del manejador de evento a cada boton (otra forma)*/
        btnAgregar = (Button) findViewById(R.id.btnAgregar);
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = listaMenu.getCount();
                SparseBooleanArray viewItems = listaMenu.getCheckedItemPositions();
                if (pedidoConfirmado == false) {
                    for (int i = 0; i < count; i++) {
                        if (viewItems.get(i)) {
                            ElementoMenu item = (ElementoMenu) listaMenu.getItemAtPosition(i);
                            Toast.makeText(getApplicationContext(), item + "..." + f.format(item.getPrecio()).toString(), Toast.LENGTH_SHORT).show();

                            /**Controlar la cantidad de cada item*/
                            if (!pertenece(elementosPedidos, item)) {
                                elementosPedidos.add(new ItemPedido(item, 1));
                            } else {
                                for (ItemPedido e : elementosPedidos) {
                                    if (e.item.equals(item)) {
                                        e.incrementarCantidad();
                                    }
                                }
                            }
                        }
                    }

                    if (elementosPedidos != null) {
                        tvDatosPedido.setText("");
                        for (ItemPedido e : elementosPedidos) {
                            tvDatosPedido.append(e.item.getNombre()+"("+e.getCantidadItem()+")"+"-> $ "+ f.format(e.item.getPrecio() * e.getCantidadItem()) +"\n");
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Debe seleccionar elementos del Menu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No puede agregar productos al pedido porque fue confirmado", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnConfirmar = (Button) findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double costo = calcularCostoPedido(elementosPedidos);
                if (pedidoConfirmado != true) {
                    if (costo != 0) {
                        tvDatosPedido.append("El costo del pedido es: $" + f.format(costo).toString());
                        Toast.makeText(getApplicationContext(), "El costo del pedidos es: $" + f.format(costo).toString(), Toast.LENGTH_SHORT).show();
                        pedidoConfirmado = true;
                    } else {
                        Toast.makeText(getApplicationContext(), "Debe seleccionar algun producto del Menu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Pedido ya confirmado con Exito", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnReiniciar = (Button) findViewById(R.id.btnReiniciar);
        btnReiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDatosPedido.setText("Datos de Pedido");
                elementosPedidos.clear();
                ArrayList<ElementoMenu> elementosPedidos = new ArrayList<ElementoMenu>();
                tvDatosPedido.setText("Datos de Pedido");
                pedidoConfirmado = false;
            }
        });
    }




    /**
     * Manejadores de Eventos
     */
    /*Manejador de eventos del ToggleButton*/
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {

        } else {

        }
    }

    /*Manejador de eventos del RadioGroupButton*/
    @Override
    public void onCheckedChanged(RadioGroup group, int id) {
        switch (id) {
            case -1:
                Log.v("NADA", "Ninguna opcion seleccionada");
                break;

            case R.id.rbtPlato:
                /**Cargar el listView con los platos de comida*/
                Log.v("PLATO", "opcion seleccionada: PLATO");
                imprimirListaItems("PLATO", listaPlatos);

                adaptador = new ArrayAdapter<ElementoMenu>(this, android.R.layout.simple_list_item_multiple_choice, listaPlatos);
                listaMenu.setAdapter(adaptador);
                break;

            case R.id.rbtBebida:
                /**Cargar el ListView con las Bebidas*/
                Log.v("BEBIDA", "opcion seleccionada: BEBIDA");

                adaptador = new ArrayAdapter<ElementoMenu>(this, android.R.layout.simple_list_item_multiple_choice, listaBebidas);
                listaMenu.setAdapter(adaptador);
                imprimirListaItems("BEBIDA", listaBebidas);
                break;

            case R.id.rbtPostre:
                /**Cargar el ListView con las Bebidas*/
                Log.v("POSTRE", "opcion seleccionada: POSTRE");

                adaptador = new ArrayAdapter<ElementoMenu>(this, android.R.layout.simple_list_item_multiple_choice, listaPostres);
                listaMenu.setAdapter(adaptador);
                imprimirListaItems("POSTRE", listaPostres);
                break;
        }
    }

    private void imprimirListaItems(String categoriaItem, ElementoMenu[] listaItems) {
        for (ElementoMenu item : listaItems) {
            Log.v(item.getId().toString(), categoriaItem + ": " + item.getNombre() + " | Precio: " + item.getPrecio());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        int count = listaMenu.getCount();
//        SparseBooleanArray viewItems = listaMenu.getCheckedItemPositions();
//
//        for (int i = 0; i < count; i++) {
//            if (viewItems.get(i)) {
//                ElementoMenu item = (ElementoMenu) listaMenu.getItemAtPosition(i);
//                Toast.makeText(getApplicationContext(), item + "..." + position, Toast.LENGTH_LONG).show();
//
//                if (!elementosPedidos.contains(item)) {
//                    elementosPedidos.add(String.valueOf(item));
//                }
//            }
//        }

//        for (int i = 0; i < count; i++) {
//            if (viewItems.get(i)) {
//                listaMenu.dispatchSetSelected(false);
//            }
//        }


//        ElementoMenu item = (ElementoMenu) listaMenu.getItemAtPosition(position);
//        Toast.makeText(getApplicationContext(), item+"..."+ position, Toast.LENGTH_LONG).show();
//        elementosPedidos.add(item.getNombre()+" ("+item.getPrecio()+")");
//
////        /**Esto podria ir en la logica del boton confirmar si se
////         * quisiera agregar todos los pedidos de una vez al textView*/
//        tvDatosPedido.setText("");
//        for(String e: elementosPedidos){
//            tvDatosPedido.append(e+"\n");
//        }
//        tvDatosPedido.setText(item.getNombre());
    }

//    @Override
//    public void onClick(View v) {
//
//
//        /**Accion del Boton Confirmar Pedido*/
//        if (btnConfirmar.isClickable()) {
//            Toast.makeText(getApplicationContext(), "Logica en construccion", Toast.LENGTH_LONG).show();
//        }
//        /**Accion del Boton Reiniciar*/
//        if (btnReiniciar.isClickable()) {
//            elementosPedidos.clear();
//            tvDatosPedido.setText("");
//            Toast.makeText(getApplicationContext(), "Pedido Reiniciado", Toast.LENGTH_LONG).show();
//        }
//    }

    double calcularCostoPedido(ArrayList<ItemPedido> listaPedido) {
        double costoPedido = 0.00;
        for (ItemPedido producto : listaPedido) {
            costoPedido += producto.item.getPrecio()*(producto.getCantidadItem());
        }

        return costoPedido;
    }

    public boolean pertenece(ArrayList<ItemPedido> listaPedido, ElementoMenu item){
        for(ItemPedido pro: listaPedido){
            if(item.getNombre().equals(pro.item.getNombre())){
                return true;
            }
        }
        return false;
    }


    private void iniciarListas() {
// inicia lista de bebidas
        listaBebidas = new ElementoMenu[7];
        listaBebidas[0] = new ElementoMenu(1, "Coca");
        listaBebidas[1] = new ElementoMenu(4, "Jugo");
        listaBebidas[2] = new ElementoMenu(6, "Agua");
        listaBebidas[3] = new ElementoMenu(8, "Soda");
        listaBebidas[4] = new ElementoMenu(9, "Fernet");
        listaBebidas[5] = new ElementoMenu(10, "Vino");
        listaBebidas[6] = new ElementoMenu(11, "Cerveza");
// inicia lista de platos
        listaPlatos = new ElementoMenu[14];
        listaPlatos[0] = new ElementoMenu(1, "Ravioles");
        listaPlatos[1] = new ElementoMenu(2, "Gnocchi");
        listaPlatos[2] = new ElementoMenu(3, "Tallarines");
        listaPlatos[3] = new ElementoMenu(4, "Lomo");
        listaPlatos[4] = new ElementoMenu(5, "Entrecot");
        listaPlatos[5] = new ElementoMenu(6, "Pollo");
        listaPlatos[6] = new ElementoMenu(7, "Pechuga");
        listaPlatos[7] = new ElementoMenu(8, "Pizza");
        listaPlatos[8] = new ElementoMenu(9, "Empanadas");
        listaPlatos[9] = new ElementoMenu(10, "Milanesas");
        listaPlatos[10] = new ElementoMenu(11, "Picada 1");
        listaPlatos[11] = new ElementoMenu(12, "Picada 2");
        listaPlatos[12] = new ElementoMenu(13, "Hamburguesa");
        listaPlatos[13] = new ElementoMenu(14, "Calamares");
// inicia lista de postres
        listaPostres = new ElementoMenu[15];
        listaPostres[0] = new ElementoMenu(1, "Helado");
        listaPostres[1] = new ElementoMenu(2, "Ensalada de Frutas");
        listaPostres[2] = new ElementoMenu(3, "Macedonia");
        listaPostres[3] = new ElementoMenu(4, "Brownie");
        listaPostres[4] = new ElementoMenu(5, "Cheescake");
        listaPostres[5] = new ElementoMenu(6, "Tiramisu");
        listaPostres[6] = new ElementoMenu(7, "Mousse");
        listaPostres[7] = new ElementoMenu(8, "Fondue");
        listaPostres[8] = new ElementoMenu(9, "Profiterol");
        listaPostres[9] = new ElementoMenu(10, "Selva Negra");
        listaPostres[10] = new ElementoMenu(11, "Lemon Pie");
        listaPostres[11] = new ElementoMenu(12, "KitKat");
        listaPostres[12] = new ElementoMenu(13, "IceCreamSandwich");
        listaPostres[13] = new ElementoMenu(14, "Frozen Yougurth");
        listaPostres[14] = new ElementoMenu(15, "Queso y Batata");
    }


    DecimalFormat f = new DecimalFormat("##.00");
    class ElementoMenu {
        private Integer id;
        private String nombre;
        private Double precio;

        public ElementoMenu() {
        }

        public ElementoMenu(Integer i, String n, Double p) {
            this.setId(i);
            this.setNombre(n);
            this.setPrecio(p);
        }

        public ElementoMenu(Integer i, String n) {
            this(i, n, 0.0);
            Random r = new Random();
            this.precio = (r.nextInt(3) + 1) * ((r.nextDouble() * 100));
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Double getPrecio() {
            return precio;
        }

        public void setPrecio(Double precio) {
            this.precio = precio;
        }

        @Override
        public String toString() {
            return this.nombre + "( " + f.format(this.precio) + ")";
        }
    }

    class ItemPedido {
        private int cantidad;
        ElementoMenu item;

        public ItemPedido(ElementoMenu item, int cantidad) {
            this.cantidad = cantidad;
            this.item = item;
        }

        public void incrementarCantidad(){
            this.cantidad+=1;
        }

        public int getCantidadItem(){
            return this.cantidad;
        }
    }
}

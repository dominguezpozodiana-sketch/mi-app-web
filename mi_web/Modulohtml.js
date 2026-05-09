// Función para cargar un HTML externo dentro de un elemento
async function cargarComponente(id, ruta) {
  const respuesta = await fetch(ruta);
  const html = await respuesta.text();
  document.getElementById(id).innerHTML = html;
}

// Cargar los módulos
async function inicializarApp() {
  const componentes = [
    cargarComponente("menu-lateral", "Htmlmodulo/Menulateral.html"),
    cargarComponente("pantalla-estadistica", "Htmlmodulo/Estadistica.html"),
    cargarComponente("pantalla-compra", "Htmlmodulo/Productos.html"),
    
    ///// Cargar componentes de formularios
    //// Productos
    ///Formulario de agreagar nuevo Producto
    cargarComponente("formulario-agregar-nuevo-producto", "Htmlmodulo/Formularios/Productos/FormularioAgregarNuevoProducto.html"),
  ];
  
  const resultados = await Promise.all(componentes);
  console.log("Todo cargado", resultados);
  iniciarAccionesCuerpoMenu();
  iniciarAccionBotonMenu();
}

inicializarApp();
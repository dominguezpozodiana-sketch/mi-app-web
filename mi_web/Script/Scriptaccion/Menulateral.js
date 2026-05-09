function iniciarAccionesCuerpoMenu() {
  // Seleccionar todos los botones y todas las pantallas
  const botones = document.querySelectorAll('.btn-panlla-menu-lateral');
  const pantallas = document.querySelectorAll('.pantallas-menulateral');
  const iconos = document.querySelectorAll('.tamano-icono');
  
  // Función para mostrar una pantalla y ocultar las demás
  function mostrarPantalla(idPantalla) {
    pantallas.forEach(pantalla => {
      if (pantalla.id === idPantalla) {
        pantalla.classList.add('activa');
      } 
      else {
        pantalla.classList.remove('activa');
      };
    });
  };
  
  function iniciarPantalla() {
    botoninicio = document.getElementById('btn-pantalla-inicio');
    botoninicio.classList.add('active');
  };
  
  function cambiarTamanoIconoSeleccionadoCuerpoMenu(idIcono) {
    iconos.forEach(icono => {
      if (icono.id === idIcono) {
        icono.classList.add('activo');
      }
      else {
        icono.classList.remove('activo');
      };
    });
  };
  
  // Asignar evento click a cada botón
  botones.forEach(boton => {
    boton.addEventListener('click', () => {
      const idIcono = boton.getAttribute('data-iconos-menu');
      cambiarTamanoIconoSeleccionadoCuerpoMenu(idIcono);
      botones.forEach(btn => {
        btn.classList.remove('active');
      });
      boton.classList.add('active');
      const idPantalla = boton.getAttribute('data-pantalla-menu-lateral');
      mostrarPantalla(idPantalla);
    });
  });
  
  mostrarPantalla('pantalla-estadistica');
  cambiarTamanoIconoSeleccionadoCuerpoMenu('icon-menu-lateral-inicio');
  iniciarPantalla();
  
};

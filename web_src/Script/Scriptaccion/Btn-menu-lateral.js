function iniciarAccionBotonMenu() {
  const menuLateral = document.getElementById('menu-lateral');
  const btnMenuLateral = document.getElementById('btn-menu-lateral');

  // Si no existen los elementos, salimos para evitar errores
  if (!menuLateral || !btnMenuLateral) {
    console.error('No se encontraron los elementos del menú o botón');
    return;
  }

  // Función para cerrar el menú
  function cerrarMenu() {
    menuLateral.classList.remove('abrir');
  }

  // Función para abrir el menú
  function abrirMenu() {
    menuLateral.classList.add('abrir');
  }

  // Alternar abrir/cerrar (para el botón)
  function toggleMenu() {
    const iconoanimado = document.getElementById('icono-abrir-menu-lateral');
    const srcIconoAnimadoOriginal = iconoanimado.src;
    const tamanomenua = document.getElementById('menu-lateral');
    const tamanomenub = tamanomenua.offsetWidth;
    if (iconoanimado.src.includes('menu1.png') && tamanomenub === 50) {
      iconoanimado.src = 'iconos/MenuLateral/boton-menu-lateral-abrir/menu2.png';
      setTimeout(() => {
        iconoanimado.src = 'iconos/MenuLateral/boton-menu-lateral-abrir/menu3.png';
      }, 800);
      menuLateral.classList.toggle('abrir');
    } else {
      if (tamanomenub === 165) {
        iconoanimado.src = 'iconos/MenuLateral/boton-menu-lateral-abrir/menu4.png';
        setTimeout(() => {
          iconoanimado.src = 'iconos/MenuLateral/boton-menu-lateral-abrir/menu1.png';
        }, 800);
        menuLateral.classList.toggle('abrir');
      };
    };
  };

  // Evento para el botón: alterna el menú y evita que el clic se propague al documento
  btnMenuLateral.addEventListener('click', (evento) => {
    evento.stopPropagation(); // Evita que este clic también cierre el menú
    toggleMenu();
  });

  // Evento global: cerrar el menú si se hace clic FUERA de él
  document.addEventListener('click', (evento) => {
    // Solo actuar si el menú está actualmente abierto
    const menuAbierto = menuLateral.classList.contains('abrir');
    if (!menuAbierto) return;

    // ¿El clic fue dentro del menú?
    const clicDentro = menuLateral.contains(evento.target);

    // Si fue fuera, cerramos
    if (!clicDentro) {
      
      toggleMenu();
    }
  });
};
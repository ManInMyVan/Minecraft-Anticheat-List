function toggleLightmode() {
   document.body.classList.toggle("light-mode");
}
function toggleDarkmode() {
   document.body.classList.toggle("dark-mode");
}
function toggleTheme() {
   toggleDarkmode()
   toggleLightmode()
}
function load() {
   if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      toggleDarkmode()
   } else {
      toggleLightmode()
   }
}

function toggleDarkmode() {
   document.body.classList.toggle("light-mode");
   document.body.classList.toggle("dark-mode");
}

function load() {
   if (window.matchMedia) {
      if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
         document.body.classList.toggle("dark-mode");
      }
   } else {
      document.body.classList.toggle("light-mode");
   }
}

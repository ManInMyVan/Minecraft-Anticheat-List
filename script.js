function toggleTheme() {
   document.body.classList.toggle("dark-mode")
   document.body.classList.toggle("light-mode")
}
async function updateRatings() {
   updateRating(83198) // AntiHaxerman
   updateRating(111260) // FoxAddition
   updateRating(112706) // Gato Anticheat
   updateRating(111084) // Ghost
   updateRating(99923) // Grim
   updateRating(112053) // LiteAntiCheat
   updateRating(113149) // Spidey Anticheat v2
   updateRating(90766) // Themis
   updateRating(48399) // Negativity v1
   updateRating(113143) // Spidey Anticheat v1
}

async function fetchData(resourceid) {
   var data
   await fetch(new Request("https://api.spiget.org/v2/resources/"+resourceid))
      .then((response) => {
         data = response.json()
      })
      .catch((error) => {
         console.error(error)
      })
   return data
}

async function updateRating(resourceid) {
   var data = await fetchData(resourceid)
   document.getElementById(resourceid+"-rating").innerHTML = '<b>'+(data.rating.average * 20).toFixed(0)+'%</b>, '+data.rating.count+' rating'
   if (data.rating.count != 1) {
      document.getElementById(resourceid+"-rating").innerHTML +='s'
   }
}
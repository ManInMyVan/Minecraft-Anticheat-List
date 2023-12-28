updateRatings([
   1368, // AntiAura v2
   83198, // AntiHaxerman
   111260, // FoxAddition
   112706, // Gato Anticheat
   111084, // Ghost
   99923, // Grim
   112053, // LiteAntiCheat
   113149, // Spidey Anticheat v2
   109375, // AntiCheat (by PixelCraft55)
   90766, // Themis
   48399, // Negativity v1
   113143, // Spidey Anticheat v1
])

async function toggleTheme() {
   document.body.classList.toggle("dark-mode")
   document.body.classList.toggle("light-mode")
}
async function fetchData(resourceid) {
   return (await fetch(new Request("https://api.spiget.org/v2/resources/"+resourceid))).json()
}

async function updateRatings(resourceids) {
   resourceids.forEach(async resourceid => {
      var data = await fetchData(resourceid)
      document.getElementById(resourceid+"-rating").innerHTML = '<b>'+(data.rating.average * 20).toFixed(0)+'%</b>, '+data.rating.count+' rating'
      if (data.rating.count != 1) {
         document.getElementById(resourceid+"-rating").innerHTML +='s'
      }
   })
}

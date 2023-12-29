// TODO: move to a dif file
const anticheats = [
   /*
    * resourceid:
    *    if set, the rating will be fetched from spiget (spigotmc api),
    *    must be on spigotmc.
    *    set it to null for it to not do so.
    */
   {
      name: "Grim",
      plaform: "Bukkit (Java)",
      status: null,
      versions: "1.8, 1.12, 1.16 - 1.20.2",
      resourceid: 99923,
      rating: null,
      price: "Free", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/99923"
      }, {
         name: "GitHub",
         url: "https://github.com/GrimAnticheat/Grim"
      }, {
         name: "Hanger",
         url: "https://hangar.papermc.io/GrimAnticheat/GrimAnticheat"
      }, {
         name: "Dedicated Site",
         url: "https://grim.ac"
      }]
   }, {
      name: "AntiAura v2",
      plaform: "Bukkit (Java)",
      status: null,
      versions: "1.8 - 1.19",
      resourceid: 1368,
      rating: null,
      price: "£12.00 (GBP)", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/1368"
      }]
   }, {
      name: "Ghost",
      plaform: "Bukkit (Java)",
      status: "Discontinued",
      versions: "1.7 - 1.20",
      resourceid: 111084,
      rating: null,
      price: "Free", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/111084"
      }]
   }, {
      name: "AntiHaxerman",
      plaform: "Bukkit (Java)",
      status: null,
      versions: "1.7 - 1.8",
      resourceid: 83198,
      rating: null,
      price: "Free", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/83198"
      },{
         name: "GitHub",
         url: "https://github.com/Tecnio/AntiHaxerman"
      }]
   }, {
      name: "Cardinal",
      plaform: "Bukkit (Java)",
      status: "Active",
      versions: "1.8 - 1.20",
      resourceid: null,
      rating: "Unknown",
      price: "Free, €19.99 (EUR)", 
      links: [{
         name: "Dedicated Site",
         url: "https://cardinalanticheat.github.io/documentation"
      },{
         name: "GitHub",
         url: "https://github.com/micartey/Cardinal-Anticheat"
      }]
   }, {
      name: "FoxAddition",
      plaform: "Bukkit (Java)",
      status: null,
      versions: "1.8 - 1.20.1",
      resourceid: 111260,
      rating: null,
      price: "Free", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/111260"
      }]
   }, {
      name: "Updated NoCheatPlus",
      plaform: "Bukkit (Java)",
      status: "Active",
      versions: "1.5 - 1.20",
      resourceid: null,
      rating: "Unknown",
      price: "Free", 
      links: [{
         name: "GitHub",
         url: "https://github.com/Updated-NoCheatPlus/NoCheatPlus"
      }]
   }, {
      name: "Themis",
      plaform: "Bukkit (Java)",
      status: null,
      versions: "1.17 - 1.20.1",
      resourceid: 90766,
      rating: null,
      price: "Free", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/90766"
      }]
   }, {
      name: "Gato Anticheat",
      plaform: "Bukkit (Java)",
      status: null,
      versions: "1.8 - 1.20.2",
      resourceid: 112706,
      rating: null,
      price: "Free", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/112706"
      }]
   }, {
      name: "LiteAntiCheat",
      plaform: "Bukkit (Java)",
      status: null,
      versions: "1.13 - 1.20",
      resourceid: 112053,
      rating: null,
      price: "Free", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/112053"
      }, {
         name: "GitHub",
         url: "https://github.com/tiredvekster/LiteAntiCheat"
      }]
   }, {
      name: "Spidey Anticheat v2",
      plaform: "Bukkit (Java)",
      status: null,
      versions: "1.8",
      resourceid: 113149,
      rating: null,
      price: "Free", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/113149"
      }]
   }, {
      name: "AntiCheat (by PixelCraft55)",
      plaform: "Bukkit (Java)",
      status: null,
      versions: "1.16 - 1.19",
      resourceid: 109375,
      rating: null,
      price: "Free", 
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/109375"
      }]
   }, {
      name: "Negativity v1",
      plaform: "Bukkit (Java)",
      status: "Discontinued",
      versions: "1.8 - 1.20.1",
      resourceid: 48399,
      rating: null,
      price: "Free",
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/48399"
      }, {
         name: "GitHub",
         url: "https://github.com/Elikill58/Negativity"
      }]
   }, {
      name: "Spidey Anticheat v1",
      plaform: "Bukkit (Java)",
      status: "Discontinued",
      versions: "1.8",
      resourceid: 113143,
      rating: null,
      price: "Free",
      links: [{
         name: "SpigotMC",
         url: "https://spigotmc.org/resources/48399"
      }]
   },
]

var index = 0
const numberofanticheats = 163
document.getElementById("number-of-anticheats").innerHTML = `Number of anticheats: ${numberofanticheats} (${anticheats.length} included)`
anticheats.forEach(async element => {

   if (element.resourceid != null && (element.status == null || element.rating == null)) {
      data = await fetchData(element.resourceid)
      if (element.status == null) element.status = `${10_368_000_000 > Date.now()-(data.updateDate*1000) ? 'Active' : 'Old'}`
      if (element.rating == null) element.rating = `<b>${(data.rating.average * 20).toFixed(0)}%</b>, ${data.rating.count} rating${data.rating.count == 1 ? '' : 's'}`
   }

   ++index

   // couldn't think of a better way to do this 
   if (index >= anticheats.length) { anticheats
      .sort(function (a, b) { 
         if (a.status == "Active" && b.status != "Active") return -1
         else if (a.status != "Active" && b.status == "Active") return 1
         else return a.name.localeCompare(b.name)
      })

      .forEach(element => {
         var links = []
         element.links.forEach(element => links.push(`<a href="${element.url}">${element.name}</a>`))
         document.getElementById("anticheat-table").innerHTML +=
            `<tr>
               <td>${element.name}</td>
               <td>${element.plaform}</td>
               <td>${element.status}</td>
               <td>${element.versions}</td>
               <td>${element.rating}</td>
               <td>${element.price}</td>
               <td>${links.join(", ")}</td>
            </tr>
            `
      })
   }
})

async function toggleTheme() {
   document.body.classList.toggle("dark-mode")
   document.body.classList.toggle("light-mode")
}

async function fetchData(resourceid) {
   return (await fetch(new Request("https://api.spiget.org/v2/resources/"+resourceid))).json()
}

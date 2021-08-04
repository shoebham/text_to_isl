function test_list()
{
    
    let ul = document.querySelector(".test_list");
    fetch('http://localhost/SPIT/Summer_project/text_to_isl/UI/js/sigmlFiles.json')
    .then(response => response.json())
     .then((data)=>
     {
         data.forEach((e)=>
         {
             let li=document.createElement("li");
            //  li.appendChild(document.createTextNode(e.name));
            li.innerHTML=`<a href="#player" onclick="setSiGMLURL('SignFiles/${e.name}.sigml');" > ${e.name}</a>`
            ul.appendChild(li);
            console.log(e.name);
         });
     });

    
}
test_list();

function setsigml(text)
{
    console.log(text);
    setSiGMLURL(`SignFiles/${text}.sigml`);
}

$('a').click(function(event){
    event.preventDefault();
    //do whatever
  });
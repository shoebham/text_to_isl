function test_list()
{
    
    let ul = document.querySelector(".test_list");
    fetch('static/js/sigmlFiles.json')
    .then(response => response.json())
     .then((data)=>
     {
         data.forEach((e)=>
         {
             let li=document.createElement("li");
            //  li.appendChild(document.createTextNode(e.name));
            li.innerHTML=`<a href="#player" onclick="setSiGMLURL('SignFiles/${e.name}.sigml');" > ${e.name}</a>`
            ul.appendChild(li);
            // console.log(e.name);
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
// write a function for factorial

//   let form = document.getElementById('form');
//   form.addEventListener('submit', function(event) {
//     event.preventDefault();    
//     document.submitForm.submit();// prevent page from refreshing
//     // const formData = new FormData(form);  // grab the data inside the form fields
//     // fetch('/', {   // assuming the backend is hosted on the same server
//     //     method: 'POST',
//     //     body: formData,
//     // }).then(function(response) {
//     //     // do something with the response if needed.
//     //     // If you want the table to be built only after the backend handles the request and replies, call buildTable() here.
//     //     console.log("response = ",response);
//     //     console.log("{{response}}");
//     //     console.log("{{result}}");
//     // });
// });


//KEEPING THIS FOR LATER 
// function send_to_back()
// {
//     let input = document.getElementById("text").value;
//     console.log("input",input);
//     $( "#form" ).submit(function( event ) {
//         event.preventDefault(); // <---- Add this line

//         $.ajax({
//             type: "POST",
//             url:"/",
//             data:{text:JSON.stringify(input)},
//             success: function(data) {
//                 // print here 
                
//                 console.log(data);
//             },
//             error: function(req, textStatus, errorThrown) {
//                 //this is going to happen when you send something different from a 200 OK HTTP
//                 alert('Ooops, something happened: ' + textStatus + ' ' +errorThrown)
//             },
//             dataType: 'json' // for json response or 'html' for html response
//         });
//     });
// }

// function display_isl_text()
// {

// }
// function play_each_word()
// {

// }
// document.getElementById('submit').addEventListener('click',()=>
// {
//     send_to_back();
// })
// send_to_back();
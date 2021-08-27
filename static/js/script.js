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

// function setsigml(text)
// {
//     console.log(text);
//     setSiGMLURL(`SignFiles/${text}.sigml`);
// }
var wordArray=[];
$('a').click(function(event){
    event.preventDefault();
    //do whatever
  });

  let form =  document.getElementById('form');
  form.addEventListener('submit', function(event) {
    event.preventDefault();
});

let sub =  document.getElementById('submit');
  sub.addEventListener('click',()=>
  {
    //   sub.preventDefault();
    let input =  document.getElementById('text').value;
    console.log("INPUT is ",input);

      $.ajax({
          url:'/',
          type:'POST',
          data:{text:input},
          success: function(res)
          {
            //   console.log(res);
            // play_each_word(res);
            ;
            convert_json_to_arr(res);
            play_each_word2();
            display_isl_text(res);
          },
          error: function(xhr)
          {
            console.log(xhr);
          }
      });
  });

  function display_isl_text(words)
  {
      let p = document.getElementById("isl_text");
      p.textContent="";
      Object.keys(words).forEach(function(key) 
      {
        p.textContent+= words[key]+" ";
      });
  }

  
function convert_json_to_arr(words)
{
    wordArray=[];
    console.log("wordArray",words);
    Object.keys(words).forEach(function(key) {
        wordArray.push(words[key]);
    });
    console.log("wordArray",wordArray);
}
function play_each_word2(){
  totalWords = wordArray.length;
  i = 0;
  var int = setInterval(function () {
      if(i == totalWords) {
          if(playerAvailableToPlay) {
              clearInterval(int);
              finalHint = $("#inputText").val();
              $("#textHint").html(finalHint);
          }
      } else if(playerAvailableToPlay) {
              playerAvailableToPlay = false;
              startPlayer("SignFiles/" + wordArray[i]+".sigml");
              console.log("CURRENTLY PLAYING",wordArray[i]);
              i++;
              playerAvailableToPlay=true;
          }
         else if(playerAvailableToPlay==false){
            let errtext = $(".statusExtra").val();
            if(errtext.indexOf("invalid") != -1) {
                playerAvailableToPlay=true;
            }
         }
  }, 1000);
};
  function play_each_word(words){
      
  Object.keys(words).forEach(function(key) {
                console.log('Key : ' + key + ', Value : ' + words[key])
                let word_to_play = words[key];
                console.log("Playing ",word_to_play);
                setSiGMLURL(`SignFiles/${word_to_play}.sigml`);    
                document.querySelector('.bttnPlaySiGMLURL').click();         
  });
}

var loadingTout = setInterval(function() {
    if(tuavatarLoaded) {
        // $("#loading").hide();
        clearInterval(loadingTout);
        console.log("Avatar loaded successfully !");
    }
}, 1500);

// function getstat (e)
// {
//     console.log(e);
// }
// console.log(CWASA.callHook('animactive'));
// console.log(avatarBusy);
// let form =  document.getElementById('form');
//   form.addEventListener('submit', function(event) {
//     event.preventDefault();    // prevent page from refreshing
//     const formData = new FormData(form);  // grab the data inside the form fields
//     fetch('/', {   // assuming the backend is hosted on the same server
//         method: 'POST',
//         body: formData,
//     }).then(function(response) {
//         console.log("LOOK HERE",response);
//         // do something with the response if needed.
//         // If you want the table to be built only after the backend handles the request and replies, call buildTable() here.
//     });
// });
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
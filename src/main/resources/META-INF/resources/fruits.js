function refresh() {
    $.get('/fruits', function (fruits) {
        var list = '';
        (fruits || []).forEach(function (fruit) { 
            list += ` 
                <tr>
                  <td>${fruit.id}</td>
                  <td><input type="text" id="updated-name${fruit.id}" value="${fruit.name}"></td>
                  <td><a href="#" onclick="deleteFruit(${fruit.id})">Delete</a></td>
                  <td><a href="#" onclick="updateFruit(${fruit.id})">Update</a></td>
                </tr>
            `;
        });
        if (list.length > 0) {
            list = ''
                + '<table><thead><th>Id</th><th>Name</th><th></th><th></th></thead>'
                + list
                + '</table>';
        } else {
            list = "No fruits in database"
        }
        $('#all-fruits').html(list);
    });
}

function deleteFruit(id) {
    $.ajax('/fruits/' + id, {method: 'DELETE'}).then(refresh);
}

function updateFruit(id) {
    var fruitName = $(`#updated-name${id}`).val();
    postData('/fruits', {id, name: fruitName})
    .then(refresh);
}

$(document).ready(function () {

    $('#create-fruit-button').click(function () {
        var fruitName = $('#fruit-name').val();
        $.post({
            url: '/fruits',
            contentType: 'application/json',
            data: JSON.stringify({name: fruitName})
        }).then(refresh);
    });

    refresh();
});

// Example POST method implementation:
async function postData(url = '', data = {}) {
  // Default options are marked with *
  const response = await fetch(url, {
    method: 'PUT', // *GET, POST, PUT, DELETE, etc.
    mode: 'cors', // no-cors, *cors, same-origin
    cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
    credentials: 'same-origin', // include, *same-origin, omit
    headers: {
      'Content-Type': 'application/json'
      // 'Content-Type': 'application/x-www-form-urlencoded',
    },
    redirect: 'follow', // manual, *follow, error
    referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
    body: JSON.stringify(data) // body data type must match "Content-Type" header
  });
  return response.json(); // parses JSON response into native JavaScript objects
}

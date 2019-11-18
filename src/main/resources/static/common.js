function urlParam(name) {
   if(name=(new RegExp('[?&]'+encodeURIComponent(name)+'=([^&]*)')).exec(location.search))
      return decodeURIComponent(name[1]);
}

function submitMatchResult(matchId, winnerId, playerName) {
    if(confirm("Do you confirm that "+playerName+" won the match?")) {
        var tournamentId = document.getElementById("tournamentIdInput").value;
        var postData = { "tournamentId" : tournamentId, "winnerId": winnerId}
        $.ajax({
            type: "PUT",
            url: "/api/match/"+matchId,
            data: JSON.stringify(postData),
            complete: function (request, textStatus) {
                reloadTree();
            },
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            traditional: true
        });
    }
}

function ratingToRow(rec) {
    if (rec.lastIncrement > 0)
        incrSpan = '<span class="positiveDelta">&#43;' + rec.lastIncrement + '</span>';
    else if (rec.lastIncrement < 0)
        incrSpan = '<span class="negativeDelta">' + rec.lastIncrement + '</span>';
    else
        incrSpan = '<span>'+rec.lastIncrement+'</span>';

    return $('<tr>')
        .append($('<td>').append(rec.name))
        .append($('<td>').append(rec.rating))
        .append($('<td>').append(incrSpan));
}

function loadRatings() {
    $.getJSON( "/api/players/ratings", function( data ) {
        $('#ratingsDiv').empty();
        if(data.length > 0) {
            var table = $('<table>').append($('<thead>').append(
                $('<tr>')
                    .append($('<td>').append("Name"))
                    .append($('<td>').append("Rating"))
                    .append($('<td>').append("Last match"))
            ));
            for (i = 0; i < data.length; i++) {
                table.append($('<tbody>').append(ratingToRow(data[i])));
            }
            $('#ratingsDiv').append(table);
        }
    });
}

function readOnly() {
    if (typeof urlParam('rw') !== 'undefined') return;
    $(".withPointer").removeClass('withPointer')
    $(".cta").prop('onclick', null);
    $("a.cta").each(function( index ) {
        var content = $(this).html()
        $(this).replaceWith(content);
    });
}

var alternativeView;
var alternativeViewName;
if (window.location.href.indexOf("tree.html") !== -1) {
    alternativeViewName = "Brackets view"
    alternativeView = window.location.href.replace("tree.html", "brackets.html");
} else if (window.location.href.indexOf("brackets.html") !== -1) {
    alternativeViewName = "Tree view"
    alternativeView = window.location.href.replace("brackets.html", "tree.html");
}

if (typeof onIndex === 'undefined') {
    $('<a href="index.html" style="position: absolute;left: 0; top: 0; display: block; margin: 20px 0px 0px 30px;">Back to home</a>').appendTo(document.body);
}

if (typeof alternativeViewName !== 'undefined') {
    $('<a href="'+alternativeView+'" style="position: absolute;right: 0; top: 0; display: block; margin: 20px 30px 0px 0px;">'+alternativeViewName+'</a>').appendTo(document.body);
}

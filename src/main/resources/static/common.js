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

function submitMatchResultAsync(matchId, winnerId, playerName) {
    if(confirm("Do you confirm that "+playerName+" won the match?")) {
        var tournamentId = document.getElementById("tournamentIdInput").value;
        var postData = { "tournamentId" : tournamentId, "winnerId": winnerId}
        $.ajax({
            type: "PUT",
            url: "/api/v2/match/"+matchId,
            data: JSON.stringify(postData),
            complete: function (request, textStatus) {
                console.log("submitted to v2!");
            },
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            traditional: true
        });
    }
}

var alternativeView;
if (window.location.href.indexOf("tree.html") !== -1) {
    alternativeView = window.location.href.replace("tree.html", "brackets.html");
} else {
    alternativeView = window.location.href.replace("brackets.html", "tree.html");
}
$('<a href="'+alternativeView+'" style="position: absolute;right: 0; top: 0; display: block; margin: 20px 30px 0px 0px;">Change view</a>').appendTo(document.body);

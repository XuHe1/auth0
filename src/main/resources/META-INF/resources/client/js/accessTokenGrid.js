var accessTokenGridView = (function() {

  var templateId = "tplAccessTokenGrid";
  var containerSelector = "#contentView";
  var handleSelector = "#accessTokenGrid";

  return {

    refresh: function(accessTokens) {
      this.hide();
      this.show(accessTokens);
    },

    show: function(accessTokens) {
      Template.get(templateId, function(template) {
        $(containerSelector).append(template({accessTokens: accessTokens}));
        $(containerSelector).css("height", ""); // clear the fixed height


        $("a.deleteAccessTokenButton").click(function(e) {
          var accessTokenId = $(e.target).closest("tr").attr("data-accessTokenId");
          bootbox.confirm("Are you sure you want to delete this Access Token?", function (result) {
            if (result) {
              accessTokenGridController.onDelete(accessTokenId);
            }
          });
          return false;
        });
      });
    },
    isVisible: function() {
      return $(handleSelector).is(':visible');
    },
    hide: function() {
      $(containerSelector).css("height", $(containerSelector).height()); // set a fixed height to prevent wild swapping of the footer
      $(handleSelector).remove();
    },
    focus: function() {
      $(handleSelector).focus();
    }
  }
})();

var accessTokenGridController = (function() {

  var view = accessTokenGridView;

  return {
    show: function() {
      // first hide to view to prevent multiple views displayed
      view.hide();
      data.getAccessTokens(function(accessTokens) {
        for (var i = 0; i < accessTokens.length; i++) {
          var accessToken = accessTokens[i];
          if (accessToken.expiresIn > 0) {
            // New date based on 'current date in ms, plus expiration-in-seconds times 1000)
            accessToken.expiresIn = new Date(new Date().getTime() + accessToken.expiresIn*1000).toLocaleString();
          } else if (accessToken.expiresIn == 0) {
            accessToken.expiresIn = '∞';
          } else {
            accessToken.expiresIn = "expired";
          }

          accessToken.creationDate = new Date(accessToken.creationDate).toLocaleString();

        }
        view.show(accessTokens);
      });
    },


    onDelete: function(accessTokenId) {
      data.deleteAccessToken(accessTokenId, function() {
        console.log("access token has been deleted.");
        windowController.onDeleteAccessToken();
      }, function (errorMessage) {
        console.log("error while saving data: " + errorMessage);
        popoverBundle.showMessage("error", errorMessage, $("#resourceServerGrid"));
      });
    },

    hide: view.hide,
    focus: view.focus,
    isVisible: view.isVisible
  }
})();


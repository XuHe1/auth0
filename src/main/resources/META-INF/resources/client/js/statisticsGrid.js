var statisticsGridView = (function() {

  var templateId = "tplStatisticsGrid";
  var containerSelector = "#contentView";
  var handleSelector = "#statisticsGrid";

  return {

    refresh: function(statistics) {
      this.hide();
      this.show(statistics);
    },

    show: function(statistics) {
      Template.get(templateId, function(template) {
        $(containerSelector).append(template(statistics));
        $(containerSelector).css("height", ""); // clear the fixed height
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

var statisticsGridController = (function() {

  var view = statisticsGridView;

  return {
    show: function() {
      // first hide to view to prevent multiple views displayed
      view.hide();
      data.getStatistics(function(statistics) {
        view.show(statistics);
      });
    },
    hide: view.hide,
    focus: view.focus,
    isVisible: view.isVisible
  }
})();


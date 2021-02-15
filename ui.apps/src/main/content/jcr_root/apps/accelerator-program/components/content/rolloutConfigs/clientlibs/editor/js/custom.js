$(document).ready(function() {
    $('#modifiedPage').multiselect({
        enableHTML : false,
        maxHeight : 400,
        buttonWidth : '200px',
        enableCaseInsensitiveFiltering : true,
        includeFilterClearBtn : true,
        includeSelectAllOption : true,
        selectAllJustVisible : false
    });
    
    $('#createdPage').multiselect({
        enableHTML : false,
        maxHeight : 400,
        buttonWidth : '200px',
        enableCaseInsensitiveFiltering : true,
        includeFilterClearBtn : true,
        includeSelectAllOption : true,
        selectAllJustVisible : false
    });
    
    $('#countries').multiselect({
        enableHTML : false,
        maxHeight : 400,
        buttonWidth : '200px',
        enableFiltering : true,
        includeFilterClearBtn : true,
        includeSelectAllOption : true,
        selectAllJustVisible : false,
        includeResetOption : true
    });

    var counter = 1;
    var addCountry = $('.country-rollout');
    var wrapper = $('.country-rollout-fields');
    var formFields = $('.new-country-form-fields').html();

    $(addCountry).click(function(){
        $(wrapper).append(formFields);
        $('#countryName').attr('id', 'countryName-'+counter).attr('name', 'countryName-'+counter);
        $('#countryTitle').attr('id', 'countryTitle-'+counter).attr('name', 'countryTitle-'+counter);
        $('#languages').attr('id', 'languages-'+counter).attr('name', 'languages-'+counter);
        $('#rolloutConfigs').attr('id', 'rolloutConfigs-'+counter).attr('name', 'rolloutConfigs-'+counter);
        $('#isDeepCountry').attr('id', 'isDeepCountry-'+counter).attr('name', 'isDeepCountry-'+counter);

        $('#rolloutConfigs-'+counter).multiselect({
            enableHTML : false,
            maxHeight : 400,
            buttonWidth : '200px'
        });
        
        $('#languages-'+counter).multiselect({
            enableHTML : false,
            maxHeight : 400,
            buttonWidth : '200px'
        });

        counter++;
    });
    
    $('.rollout').click(function(){
    	var data = {};
    	data.createdPage = $('#createdPage').val();
    	data.modifiedPage = $('#modifiedPage').val();
    	data.countries = $('#countries').val();
    	data.isDeep = $('#defaultCheck1:checked').length > 0;
    	data.templatePath = $('#countryTemplatePath').val();
    	data.siteRootPath = $('#siteRootPath').val();
    	
    	var newCountryDetails = [];
        console.log(counter);
    	for(var i = 1; i<counter; i++){
    		var countryDetails = {};
    		countryDetails.name = $('#countryName-'+i).val();
	    	countryDetails.title = $('#countryTitle-'+i).val();
	    	countryDetails.rolloutConfigs = $('#rolloutConfigs-'+i).val();
	    	countryDetails.languages = $('#languages-'+i).val();
	    	countryDetails.isDeep = $('#isDeepCountry-'+i+':checked').length > 0;
	    	newCountryDetails.push(countryDetails)
    	}
    	data.newCountryDetails = newCountryDetails;
    	console.log(data);

        $.ajax({
              type: 'POST',
              url: "/someaction",
              data: JSON.stringify(data),
              dataType: "json",
              success: function(resultData) { alert("Save Complete") }
        });

    })
    
});

var caseTable
var jjoTable
var judgmTable
var pressTable
var appealTable
var judgmCasesDel
var judgmFilesDel
var judgmFilesAdd
var judgmFilesUpd
var judgmTitlesDel
var judgmTitlesAdd
var judgmTitlesUpd
var judgmJjosDel
//var judgmJjosAdd
//var judgmJjosUpd
var judgmAplsDel

var fileRefs

var isConfirmed = false
var isRevoked = false
var ncnUrgentOldValue;

var appealSearchNcnRlt;

var judgmXfrId;

var fileInput = function(cell, onRendered, success, cancel, editorParams) {

	if (isRevoked || isConfirmed)
		return;
	console.log(cell.getRow().getData().fileName)
	//console.log(cell.getData().tmp)
	//console.log(cell.getRow().getData().fileName.indexOf('\\'))
	if (typeof cell.getRow().getData().judgmFileId !== "undefined" && cell.getRow().getData().fileName !== null) {
		if (cell.getRow().getData().fileName.indexOf('\\') < 0)
			return
	}

	var hddt = new Date(Date.parse($('#datepicker').val() + " " + $('#timeentry').val().replace(/(AM|PM)/i, " $1")));

	var curdt = new Date();

	if (curdt < hddt) {
		//alert("Only allow to upload on or after Hand down date time")
		return
		//editor.setAttribute("display", "none");
	}


	var cellId = cell.getRow().getData()._id;
	var editor = document.createElement("input");
	editor.setAttribute("type", "file");
	editor.setAttribute("name", "imgFile");
	editor.setAttribute("id", cellId + "_imgFile");
	editor.style.padding = "3px";
	editor.style.width = "100%";
	editor.style.boxSizing = "border-box";
	onRendered(function() {
		editor.focus();
		editor.style.css = "100%";
	});


	function clickFunc(event) {

		var hddt = new Date(Date.parse($('#datepicker').val() + " " + $('#timeentry').val().replace(/(AM|PM)/i, " $1")));

		var curdt = new Date();

		if (curdt < hddt) {
			event.preventDefault();
			//alert("Only allow to upload on or after Hand down date time")
			return

		}
	}

	function successFunc() {
		//console.log(cell, editor.value, editor.files[0])
		if (editor.value != "") {
			// cell.setValue(editor.value, true)
			success(editor.value);
			//console.log(cell, editor.value, editor.files[0])
			file = new Object()
			file.customId = cell.getRow().getData().fileType + cell.getRow().getPosition()
			file.value = editor.value // file path
			file.file = editor.files[0] // file data
			updFile = false
			if (fileRefs.length == 0)
				fileRefs.push(file)
			else {
				for (var i in fileRefs) {
					if (fileRefs[i].customId == file.customId) {
						fileRefs[i].file = file.file;
						fileRefs[i].value = file.value;
						updFile = true;
						break;
					}
				}
				if (!updFile)
					fileRefs.push(file)
			}
		} else {
			cell.cancelEdit();
		}
	}
	//editor.addEventListener("click", clickFunc);
	editor.addEventListener("change", successFunc);
	return editor;
}
var customEditor = function(cell, onRendered, success, cancel, editorParams) {
	if (isRevoked || isConfirmed)
		return;

	var editor = document.createElement("input");
	editor.style.width = "100%";
	//editor.style.height = "35px";
	//editor.value = cell.getValue()
	editor.value = cell.getRow().getData().caseCourtSys +
		cell.getRow().getData().caseType +
		cell.getRow().getData().caseSerNo +
		"/" + cell.getRow().getData().caseYr
	onRendered(function() {
		editor.style.css = "100%";
		editor.focus();
		$(editor).inputmask({
			regex: "^[a-zA-Z]{3,4}[0-9]{1,6}\/[1-2][0-9]{3}$",
			casing: 'upper',
		});
		$(editor).trigger('click')
	});

	function successFunc() {
		console.log("+1 " + editor.value)
		console.log("+2 " + cell.value)
		console.log("+3 " + cell.getValue())
		matchResult = parseCaseNo(editor.value.toUpperCase());
		caseCourtSys = matchResult[1];
		caseSerNo = matchResult[3]
		caseType = matchResult[2]
		caseYr = matchResult[4]
		caseno = caseCourtSys + caseType + caseSerNo + "/" + caseYr;
		cell.getData().caseCourtSys = caseCourtSys.toUpperCase()
		cell.getData().caseType = caseType.toUpperCase()
		cell.getData().caseSerNo = caseSerNo
		cell.getData().caseYr = caseYr
		success(editor.value.toUpperCase());
	}
	editor.addEventListener("blur", successFunc);
	editor.addEventListener("change", successFunc);
	return editor;
}


function validateTitles() {

	// judgment file only

	jf = judgmTable.getData().filter(function(value, index, arr) {
		return value.fileType == 'J';
	});

	for (const case0 of caseTable.getRows()) {


		caseno = case0.getCells()[0].getElement().innerText
		caseid = case0.getData().judgmCaseId
		// for each lang

		for (const file0 of jf) {
			title = false;
			if (typeof file0.judgmCaseTitles == 'undefined') {
				displayError('Please enter ' + file0.lang + ' Title for ' + caseno)
				return false;

			}

			if (file0.judgmCaseTitles.length == 0) {
				displayError('Please enter ' + file0.lang + ' Title for ' + caseno)
				return false;
			}
			for (const title0 of file0.judgmCaseTitles) {
				if (title0.caseno == caseno && title0.title !== "") {

					title = true;
				}
				else if (title0.judgmCaseId == caseid && title0.title !== "") {
					console.log('here')
					title = true;
				}
			}
			if (!title) {
				displayError('Please enter ' + file0.lang + ' Title for ' + caseno)
				return false;

			}
		}
	}

	return true;

}

function validateUpdateJudgmFile() {

	console.log("validateUpdateJudgmFile")
	// judgmFile
	// press summary
	jf = judgmTable.getData().filter(function(value, index, arr) {
		return value.fileType == 'J';
	});
	for (const file0 of jf) {
		console.log(file0)
		if (file0.otType == "") {
			displayError("Please choose Orig/Trans for " + (file0.lang !== "" ? file0.lang : '') + " judgment file")
			return false
		}
		if (file0.lang == "") {
			displayError("Please choose Language for " + (file0.otType == "O" ? "original" : "translated") + " judgment file")
			return false
		}
	}
	// press summary
	pf = pressTable.getData().filter(function(value, index, arr) {
		return value.fileType == 'P';
	});

	for (const file0 of pf) {
		console.log(file0)
		if (file0.otType == "") {
			displayError("Please choose Orig/Trans for " + (file0.lang !== "" ? file0.lang : '') + " press summary file")
			return false
		}
		if (file0.lang == "") {
			displayError("Please choose Language for " + (file0.otType == "O" ? "original" : "translated") + " press summary  file")
			return false
		}
	}

	if (!cntJudgmLang(judgmTable, "J"))
		return false

	if (!cntJudgmLang(pressTable, "P"))
		return false

	return true
}

function validateConfirmJudgmFile() {

	console.log("validateConfirmJudgmFile")
	// judgmFile
	jf = judgmTable.getData().filter(function(value, index, arr) {
		return value.fileType == 'J';
	});

	if (jf.length == 0) {
		displayError('Please add Judgment file')
		return false;
	}
	for (const file0 of jf) {
		if (typeof file0.judgmFileId !== "undefined") {
			var fileDeleted;

			for (const delFile of judgmFilesDel) {
				fileDeleted = false;
				if (delFile.judgmFileId == file0.judgmFileId) {
					console.log('skip validation of deleted file: ' + file0.judgmFileId)
					fileDeleted = true;
					break;
				}
			}
			if (fileDeleted)
				continue;
		}
		console.log(file0)
		if (file0.otType == "") {
			displayError("Please choose Orig/Trans for " + (file0.lang !== "" ? file0.lang : '') + " " + (file0.fileType == 'J' ? "Judgment" : "Press Summary") + " file")
			return false
		}
		if (file0.lang == "") {
			displayError("Please choose Language for " + (file0.otType == "O" ? "original" : "translated") + " " + (file0.fileType == 'J' ? "Judgment" : "Press Summary") + " file")
			return false
		}

		if (file0.fileName == null || file0.fileName == "") {
			displayError("Please upload " + (file0.otType == "O" ? "original" : file0.lang) + " " + (file0.fileType == 'J' ? "Judgment" : "Press Summary") + " file")
			return false
		}

	}

	// press summary
	pf = pressTable.getData().filter(function(value, index, arr) {
		return value.fileType == 'P';
	});

	for (const file0 of pf) {
		if (typeof file0.judgmFileId !== "undefined") {
			var fileDeleted;

			for (const delFile of judgmFilesDel) {
				fileDeleted = false;
				if (delFile.judgmFileId == file0.judgmFileId) {
					console.log('skip validation of deleted file: ' + file0.judgmFileId)
					fileDeleted = true;
					break;
				}
			}
			if (fileDeleted)
				continue;
		}
		console.log(file0)
		if (file0.otType == "") {
			displayError("Please choose Orig/Trans for " + (file0.lang !== "" ? file0.lang : '') + " " + (file0.fileType == 'J' ? "Judgment" : "Press Summary") + " file")
			return false
		}
		if (file0.lang == "") {
			displayError("Please choose Language for " + (file0.otType == "O" ? "original" : "translated") + " " + (file0.fileType == 'J' ? "Judgment" : "Press Summary") + " file")
			return false
		}

		if (file0.fileName == null || file0.fileName == "") {
			displayError("Please upload " + (file0.otType == "O" ? "original" : file0.lang) + " " + (file0.fileType == 'J' ? "Judgment" : "Press Summary") + " file")
			return false
		}

	}

	if (!cntJudgmLang(judgmTable, "J"))
		return false
	if (!cntJudgmLang(pressTable, "P"))
		return false
	//if (!validateTitles())
	//	return false
	return true
}


$(function() {
	$('#copy-btn').tooltip();

	$('#copy-btn').click(function() {
		var el = $(this);
		xxx(el);
	});

	$('#btn-canceledit').click(function() {
		window.location.href = "/ncns/main"
	});



	// initialize controls
	$("#inputGroupSelect03").inputmask({
		mask: '9999',
		placeholder: '',
		showMaskOnHover: false,
		showMaskOnFocus: false,
		onBeforePaste: function(pastedValue, opts) {
			var processedValue = pastedValue;
			//do something with it
			return processedValue;
		}
	});
	$('#datepicker').datepicker({
		uiLibrary: 'bootstrap5',
		format: 'yyyy/mm/dd',
		todayBtn: true

	});
	$('#timeentry').timeEntry({
		show24Hours: false,
		timeSteps: [1, 1, 0]
	});
	$('#flexRadioResNcn').change(
		function() {
			$('#InputResNcn').prop('disabled', false);
			$('#InputResCaseNo').val('')
			$('#InputResCaseNo').prop('disabled', true);
			$('.input-group.mb-3 input,.input-group.mb-3 select').prop(
				'disabled', false);
			$('#btn-newncn').prop('disabled', false);
		})
	$('#flexRadioResCaseNo').change(
		function() {

			$('#InputResCaseNo').prop('disabled', false);
			$('#InputResCaseNo').focus()
			$('#InputResNcn').val('')
			$('#inputGroupSelect01').val(new Date().getFullYear())
			$('#inputGroupSelect03').val('')
			$('#InputResNcn').prop('disabled', true);
			$('.invalid-feedback').hide()
			$('#btn-newncn').prop('disabled', true);
			$('.input-group.mb-3 input,.input-group.mb-3 select').prop(
				'disabled', true);
		})
	$('#radioSearchAppealNcn').change(
		function() {
			$('#inputSearchAppealCase').val('')
			$('#inputSearchAppealCase').prop('disabled', true);
			$('#sltSearchAppealNcnYr, #sltSearchAppealNcnCourtPrefix,#inputSearchAppealNcnSerno').prop(
				'disabled', false);

		})
	$('#radioSearchAppealCase').change(
		function() {

			$('#inputSearchAppealCase').prop('disabled', false);
			$('#inputSearchAppealCase').focus()
			$('#inputSearchAppealNcnSerno').val('')
			$('#sltSearchAppealNcnYr').val(new Date().getFullYear())
			$('#inputSearchAppealNcnSerno').prop('disabled', true);


			$('#sltSearchAppealNcnYr, #sltSearchAppealNcnCourtPrefix,#nputSearchAppealCase').prop(
				'disabled', true);
		})
	$('#btn-modal-revokencn').on('click',
		function() {

			revokeNcn()

		})


	$('#btn-confirmncn').click(
		function() {
			if (!validateUpdateJudgmFile())
				return


			if (!validateConfirmJudgmFile())
				return;


			if (!validateTitles())
				return

			ncnno = $('#ncn-no b').html()
			$("#modal-confirmncn #alert-msg2").html(
				'Confirm <b>' + ncnno + '</b> ?')
			//document.getElementById("overlay").style.display = "flex";
			var myModal = new bootstrap.Modal(document
				.getElementById('modal-confirmncn'), {
				keyboard: false
			})
			myModal.show()
			//revokeNcn()
		})

	$("#btn-newncn").on('click',
		function() {
			fileRefs = new Array();
			//judgmJjosAdd = new Array();



			$("#search-panel input[type=radio]").prop('disabled', true);
			$("#search-panel select").prop('disabled', true);
			$("#search-panel input[type=text]").prop('readonly', true);
			$("#search-panel button").prop('disabled', true);


			$('#print-btn').hide()
			$('#copy-btn').hide()

			tableCasetitles = [];
			isConfirmed = false
			isRevoked = false;
			if ($('#inputGroupSelect01').val().trim() == '') {
				$('.invalid-feedback.ncnyear').show()
				return;
			}
			ncnyear = $('#inputGroupSelect01').val()
			ncntype = $('#inputGroupSelect02').val()
			if (ncnyear < new Date().getFullYear()) {
				displayError('Only allow to create NCN of current year')
				return;
			}
			ncnyear = $('#inputGroupSelect01').val()
			ncntype = $('#inputGroupSelect02').val()
			$('#inputGroupSelect03').val('')
			clearall();
			$('#ncn-no').html(
				'<b>[' + ncnyear + '] ' + ncntype +
				' __</b> (<b>Status:</b> New)')
			$('#ncnYr').val(ncnyear)
			$('#ncnCrt').val(ncntype)
			showResult();
			$("#savencn").hide()
			$("#createncn").show()
			$('#btn-revokencn').hide()
			$('#btn-confirmncn').hide()
			//('button[id^=del-]').prop('disabled', true);
			//$('#btn-canceledit').hide()

		})
	function bindevent_casesearch_row() {
		$("#modal-casesearch tbody tr")
			.click(
				function() {
					// query the ncn
					ncnyear = $(this).find("input[name=ncnYear]").val();
					ncncrt = $(this).find("input[name=ncnCrt]").val();
					ncntseqno = $(this).find("input[name=ncnNo]").val();
					funcId = $("#funcId").val();
					$.ajax({
						url: "/ncns/ncnByNcn/" + ncnyear + '/' + ncncrt + '/' + ncntseqno,
						type: "GET",
						dataType: "json",
						contentType: 'application/json',
						crossDomain: true
					})
						.done(function(json) {
							console.log(json.data.judgm)
							clearall()
							if (json.data.judgm == "") {
								displayError(json.message);
								document
									.getElementById("overlay").style.display = "none";
								return;
							}

							$("#search-panel input[type=radio]").prop('disabled', true);
							$("#search-panel select").prop('disabled', true);
							$("#search-panel input[type=text]").prop('readonly', true);
							$("#search-panel button").prop('disabled', true);

							$('#print-btn').show()
							$('#copy-btn').show()
							populateNcnForm(json)
							$('#modal-casesearch').modal('hide')
						})
						.fail(function(xhr, status, errorThrown) {
							clearall()
							//alert( "Sorry, there was a problem!" );
							console.log(errorThrown);
							console.log("Request failed: " +
								status);
							console.dir(xhr.responseJSON);
							console.log('fail');
						})
						.always(function(xhr, status) {
							//document.getElementById("overlay").style.display = "none";
							//setTimeout(showResult, 300);
						});
				});

		document.getElementById("overlay").style.display = "flex";
		//setTimeout(showResult, 500);
	}
	// initialize tables
	caseTable = new Tabulator('#table-caseno', {
		placeholderLoading: "Loading List...",
		placeholderEmpty: "No Results Found",
		//reactiveData:true, 
		maxHeight: "100%",
		//minHeight:"200px",
		selectable: 1,
		layout: "fitDataStretch",
		resizableColumnFit: false,
		//data:row.getData().judgmCases,
		rowFormatter: function(row) {
			//row - row component
			var data = row.getData();
			if (typeof data.judgmCaseId == "undefined") {
				if ($('#judgmId').val() !== "")
					row.getElement().classList.add("new-row")
			}
		},
		columns: [{
			title: "Case No",
			field: "tmp",
			headerSort: false,
			validator: ["required", "minLength:2"],
			minWidth: 200
			//,editable:caseEditCheck
			,
			editable: false,
			editor: customEditor,
			cellDblClick: function(e, cell) {

				var data = cell.getRow().getData();
				if (typeof data.judgmCaseId == "undefined")
					cell.edit(true)
			},
			formatter: function(cell, formatterParams, onRendered) {
				if (typeof cell.getData().judgmCaseId !== "undefined") {
					case_no = cell.getRow().getData().caseCourtSys +
						cell.getRow().getData().caseType +
						cell.getRow().getData().caseSerNo +
						"/" + cell.getRow().getData().caseYr
					return case_no
				} else
					return cell.getValue().toUpperCase();
			}
		},
		{
			title: "primary",
			field: "primary",
			headerSort: false,
			hozAlign: "center",
			headerHozAlign: "center",
			editor: "list",
			editorParams: {
				values: {
					"Y": "YES",
					"N": "NO"
				}
			},
			formatter: function(cell, formatterParams, onRendered) {
				if (cell.getValue() == 'Y')
					return 'YES';
				else if (cell.getValue() == 'N')
					return 'NO';
			},
			editable: false,
			cellDblClick: function(e, cell) {
				var data = cell.getRow().getData();
				if (typeof data.judgmCaseId == "undefined")
					cell.edit(true)
			},
		},
		],
	})
	var titleTable
	var getOptions = function(cell) {

		var s = [
			{
				"key": "EN",
				"name": "EN"
			},
			{
				"key": "TC",
				"name": "TC"
			},
			{
				"key": "SC",
				"name": "SC"
			}];



		var filtered = new Array()

		console.log(cell.getTable().getData())


		/*for (const data of cell.getTable().getData())
		{
			s = s.filter(function(value, index, arr){ 
					console.log(data.lang, value.key)
				return data.lang !== value.key;
			});
		}*/

		var optionList = Object()

		for (const data of s) {
			optionList[data.key] = data.name
		}

		return { values: optionList };
	}
	var comboEditor = function(cell, onRendered, success, cancel) {
		//code = cell.getRow().getData()["field_name"];
		//console.log(getDropDown)
		var cboData = [
			{
				"key": "EN",
				"name": "EN"
			},
			{
				"key": "TC",
				"name": "TC"
			},
			{
				"key": "SC",
				"name": "SC"
			}];
		var editor = document.createElement("select");
		for (var i = 0; i < cboData.length; i++) {
			var opt = document.createElement('option');
			opt.value = cboData[i].key;
			opt.innerHTML = cboData[i].name;
			editor.appendChild(opt);
		}
		editor.style.padding = "3px";
		editor.style.width = "100%";
		editor.style.boxSizing = "border-box";
		editor.value = cell.getValue();

		onRendered(function() {
			editor.focus();
			$(editor).trigger('click')
			editor.style.css = "100%";
		});

		function successFunc() {
			success(editor.value);
		}

		editor.addEventListener("change", successFunc);
		editor.addEventListener("blur", successFunc);

		return editor;

	}

	caseTable.on("rowDeselected	", function(e, row) {
		$('#panel-title').hide()
	})
	caseTable.on("rowClick", function(e, row) {

		if (judgmTable.getSelectedRows().length >= 0) {
			r = judgmTable.getSelectedRows()[0]
			r.deselect()
			r.select()
		}
		else
			$('#panel-title').hide()


	})
	jjoTable = new Tabulator('#table-judgmjjo', {
		maxHeight: "100%",
		index: "judgmJjoId",
		layout: "fitColumns",
		history: true,
		selectable: 1,
		selectableCheck: function(row) {
			//row - row component
			return (!isRevoked && !isConfirmed)
		},
		rowFormatter: function(row) {
			var data = row.getData();
			if (typeof data.judgmJjoId == "undefined") {
				if ($('#judgmId').val() !== "")
					row.getElement().classList.add("new-row")
			}
		},
		//data:row.getData().judgmJjos,
		columns: [{
			title: "Name",
			editor: 'input',
			field: "jjoTitle",
			headerSort: false,
			editable: editEnable,
			validator: ["required", "minLength:2"],
			editable: false,
			cellDblClick: function(e, cell) {
				if (!editEnable()) return
				cell.edit(true);
			}
		},]
	})
	$('#add-jjorow').on("click", function() {
		jjoTable.addRow({ title: "" }).then((row) => {
			row.select();
			setTimeout(function() {
				row.getCells()[0].edit(1);
			}, 250);
		});

	});
	$('#add-fileRow').on("click", function() {


		s = judgmTable.getData().filter(function(value, index, arr) {
			return value.fileType == 'J';
		});

		if (s.length == 3) {
			displayError('Max. 3 files for a Judgment')
			return
		}

		judgmTable.addRow({
			fileType: 'J'
			, fileName: ''
			, lang: ''
			, otType: ''
		}).then((row) => {
			judgmTable.deselectRow()
			row.select();
			setTimeout(function() {
				row.getCells()[0].edit(1);
			}, 150);
		});
	});
	$('#add-pressRow').on("click", function() {


		s = pressTable.getData().filter(function(value, index, arr) {
			return value.fileType == 'P';
		});
		if (s.length == 3) {
			displayError('Max. 3 files for Press Summary')
			return
		}



		pressTable.addRow({
			fileType: 'P'
			, fileName: ''
			, lang: ''
			, otType: ''
		}).then((row) => {
			pressTable.deselectRow()
			row.select();
			setTimeout(function() {
				row.getCells()[0].edit(1);
			}, 150);
		});
	});
	$('#add-titlerow').on("click", function() {



		// Title for new Case / File
		if (typeof caseTable.getSelectedRows()[0].getData().judgmCaseId == "undefined"
			|| typeof judgmTable.getSelectedRows()[0].getData().judgmFileId == "undefined") {
			var caseCellData = caseTable.getSelectedRows()[0].getCells()[0].getData()
			jkey = caseCellData.caseCourtSys
				+ caseCellData.caseType
				+ caseCellData.caseSerNo
				+ "/" + caseCellData.caseYr


			for (let d of judgmTable.getSelectedRows()[0].getData().titleTable.getRows()) {
				if (jkey == d.getData().caseno && typeof d.getData().title !== "undefined") {
					//console.log(d.getData())
					return
				}
			}
		}
		else {
			console.log(judgmTable.getSelectedRows()[0].getData().titleTable.getRows().length)


			// going to create 
			jkey2 = caseTable.getSelectedRows()[0].getData().judgmCaseId + " "
				+ judgmTable.getSelectedRows()[0].getData().judgmFileId

			console.log('2:' + jkey2);

			for (let t of judgmTable.getSelectedRows()[0].getData().judgmCaseTitles) {
				jkey1 = t.judgmCaseId + " " + t.judgmFileId
				//console.log(judgmTable.getSelectedRows()[0].getData().judgmCaseTitles[r])
				console.log('1:' + jkey1);

				if (jkey1 == jkey2) {
					console.log('none')
					return
				}
			}
		}


		judgmTable.getSelectedRows()[0].getData().titleTable.addRow({ title: "" }).then((row) => {
			row.getTable().deselectRow()
			//row.select();
			setTimeout(function() {
				row.getCells()[0].edit(1);
			}, 150);
		});
		//caseTable.getSelectedRows()[0].update
	});
	$('#del-titlerow').on("click", function() {
		if (caseTable.getSelectedRows()[0].getData().titleTable.getSelectedRows().length == 0) {
			alert("Please select Title to be deleted.")
			return;
		} else if (caseTable.getSelectedRows()[0].getData().titleTable.getSelectedRows().length > 0) {
			if (!confirm("Remove the selected Title?"))
				return;
		}

		caseTable.getSelectedRows()[0].getData().titleTable.getSelectedRows()[0].delete()
	});
	$('#del-caserow').on("click", function() {
		if (caseTable.getSelectedRows().length == 0) {
			alert("Please select Case to be deleted.")
			return;
		} else if (caseTable.getSelectedRows().length > 0) {
			if (!confirm("Remove the selected Case?"))
				return;
		}
		for (var i in caseTable.getSelectedRows()) {
			caseTable.getSelectedRows()[i].delete()
		}
		$('#panel-title').hide()
	});
	$('#del-jjorow').on("click", function() {
		if (jjoTable.getSelectedRows().length == 0) {
			alert("Please select JJO to be deleted.")
			return;
		} else if (jjoTable.getSelectedRows().length > 0) {
			if (!confirm("Remove the selected JJO?"))
				return;
		}
		for (var i in jjoTable.getSelectedRows()) {
			jjoTable.getSelectedRows()[i].delete()
		}
	});
	$('#del-appealrow').on("click", function() {
		if (appealTable.getSelectedRows().length == 0) {
			alert("Please select Appeal to be deleted.")
			return;
		} else if (appealTable.getSelectedRows().length > 0) {
			if (!confirm("Remove the selected Appeal?"))
				return;
		}
		for (const r of appealTable.getSelectedRows()) {
			r.delete()
		}
	});
	$('#del-fileRow').on("click", function() {
		if (judgmTable.getSelectedRows().length == 0) {
			alert("Please select Judgment file to be deleted.")
			return;
		}
		else if (judgmTable.getSelectedRows().length > 0) {
			if (!confirm("Remove the selected Judgment file?"))
				return
		}
		for (var i in fileRefs) {
			console.log(fileRefs[i].value)
		}
		$('#panel-title').hide()
		console.log('delete file row')
		selectdFilename = ""
		// only delete the new file
		if (typeof judgmTable.getSelectedRows()[0].getData().judgmFileId == "undefined") {
			selectdFilename = judgmTable.getSelectedRows()[0].getData().fileName;
			console.log("selectdFilename: " + selectdFilename)
			for (const [i, v] of fileRefs.entries()) {
				// remove the item in fileRefs array
				console.log('fileRef: ' + v.value)
				if (selectdFilename == v.value) {
					console.log('delete fileRefs ' + selectdFilename)
					fileRefs.splice(i, 1)
				}
			}
		} else { }
		judgmTable.getSelectedRows()[0].delete()
		// remove any item in array judgmTitlesAdd
		//}
	});
	$('#del-pressRow').on("click", function() {
		console.log('delete press row')

		if (pressTable.getSelectedRows().length == 0) {
			alert("Please select Press Summary file to be deleted?")
			return;
		}
		else if (pressTable.getSelectedRows().length > 0) {
			if (!confirm("Remove the selected Press Summary file"))
				return
		}
		selectdFilename = ""
		// only delete the new file
		if (typeof pressTable.getSelectedRows()[0].getData().judgmFileId == "undefined") {
			selectdFilename = pressTable.getSelectedRows()[0].getData().fileName;
			console.log("selectdFilename: " + selectdFilename)
			for (const [i, v] of fileRefs.entries()) {
				// remove the item in fileRefs array
				console.log('fileRef: ' + v.value)
				customId = pressTable.getSelectedRows()[0].getData().fileType + pressTable.getSelectedRows()[0].getPosition()
				//if (selectdFilename == v.value) {
				if (customId == v.customId) {
					console.log('delete fileRefs ' + selectdFilename)
					fileRefs.splice(i, 1)
				}
			}
		} else { }
		pressTable.getSelectedRows()[0].delete()
		// remove any item in array judgmTitlesAdd
		//}
	});
	// Events
	caseTable.on("rowDeleted", function(row) {
		//row - row component
		if (row.getData().judgmCaseId != null)
			judgmCasesDel.push(row.getData());
	})
	caseTable.on("cellEdited", function(cell) {

		console.log("cellEdited: " + cell.getOldValue())
		console.log("cellEdited: " + cell.getValue())
		// remove the item judgmTitlesAdd
		for (const title of judgmTitlesAdd) {

			// filter logic
			/*checkcaseno = false;
			for (const case0 of caseTable.getData()) {
				if (typeof case0.judgmCaseId == "undefined") {
					checkcaseno = case0.tmp
				}
				else checkcaseno = case0.ncnCrt + case0.caseSerNo + "/" + case0.caseYr

				if (checkcaseno == title.caseno)
					checkcaseno = true;

			}
			*/

		}


		this.redraw()

	});


	jjoTable.on("rowAdded", function(row) {

		//judgmJjosAdd.push(row.getData());

	});
	/*    jjoTable.on("cellEdited", function(cell) {
			if (typeof cell.getRow().getData().judgmJjoId == "undefined")
				return;
			judgmJjosUpd = new Array()
			for (var c in this.getEditedCells()) {
				if (this.getEditedCells()[c].getInitialValue() !== this.getEditedCells()[c].getValue())
					judgmJjosUpd.push(this.getEditedCells()[c].getData())
			}
		});
	*/
	jjoTable.on("rowDeleted", function(row) {
		//row - row component
		if (row.getData().judgmJjoId != null)
			judgmJjosDel.push(row.getData());
		// remove any item in array judgmTitlesAdd
		/*for (var i in judgmJjosAdd) {
			if (judgmJjosAdd[i].title == row.getData().title)
				judgmJjosAdd.splice(i, 1);
		}
		*/

		// remove any item in array judgmTitlesUpd
		/*for (var i in judgmTitlesUpd)
		{
			if (judgmTitlesUpd[i].lang == row.getData().lang
				&& judgmTitlesUpd[i].title == row.getData().title)
				
			judgmTitlesUpd.splice(i, 1);

		}
		*/
		// remove any item in judgmCaseTitles data 
		/*for (var i in caseTable.getSelectedRows()[0].getData().judgmCaseTitles)
		{
			if (caseTable.getSelectedRows()[0].getData().judgmCaseTitles[i].lang == row.getData().lang
					&& caseTable.getSelectedRows()[0].getData().judgmCaseTitles[i].title == row.getData().title)
			
				caseTable.getSelectedRows()[0].getData().judgmCaseTitles.splice(i, 1);
							
		}
		*/
	});



	var caseEditCheck = function(cell) {
		var data = cell.getRow().getData();
		console.log("caseEditCheck " + typeof data.judgmCaseId)
		return (typeof data.judgmCaseId == "undefined")
	}
	var fileEditCheck = function(cell) {
		var data = cell.getRow().getData();
		return (typeof data.judgmFileId == "undefined")
	}
	var noDivide = function(cell, value, parameters) {
		//cell - the cell component for the edited cell
		//value - the new input value of the cell
		//parameters - the parameters passed in with the validator
		alert("ss")
		console.log(judgmTable.getData())
		return false
	}

	var comboEditor2 = function(cell, onRendered, success, cancel, editorParams) {
		var editor = document.createElement("select");
		for (var i = 0; i < editorParams.length; i++) {
			var opt = document.createElement('option');
			//opt.value = editorParams[i].key;
			//opt.innerHTML = editorParams[i].name;

			isSelect = false;
			//for (const f of judgmTable.getData())
			console.log(cell.getTable().getData())
			console.log(cell.getTable().element.id)
			if (cell.getTable().element.id == 'table-judgmfile')
				fileType = 'J'
			else if (cell.getTable().element.id == 'table-pressfile')
				fileType = 'P'

			s = cell.getTable().getData().filter(function(value, index, arr) {
				return value.fileType == fileType;
			});
			console.log(fileType)
			console.log(s)
			for (const f of s) {
				//if (editorParams[i].key==f.otType && f.otType =="O" && cell.getValue()!=='O')
				if (editorParams[i].key == f.otType && f.otType == "O" && cell.getValue() !== 'O') {
					isSelect = true;
					break;
				}
			}

			if (isSelect) continue;

			opt.style.fontSize = "18px";
			opt.value = editorParams[i].key;
			opt.innerHTML = editorParams[i].name;
			editor.appendChild(opt);
		}

		editor.style.padding = "2px";
		editor.style.width = "100%";
		editor.style.boxSizing = "border-box";

		editor.value = cell.getValue();

		onRendered(function() {
			editor.focus();
			editor.style.css = "100%";
		});

		function successFunc() {
			success(editor.value);
		}

		editor.addEventListener("change", successFunc);
		editor.addEventListener("blur", successFunc);

		return editor;
	};

	var cboData = [

		{
			"key": "O",
			"name": "Orig"
		},
		{
			"key": "T",
			"name": "Trans"
		}];

	judgmTable = new Tabulator('#table-judgmfile', {
		maxHeight: "100%",
		validationMode: "blocking",
		layout: "fitDataStretch",
		selectable: 1,
		//data:row.getData().judgmFiles,
		history: true,
		columns: [{
			title: "Orig/Trans",
			field: "otType",
			headerSort: false,
			editor: comboEditor2,
			editorParams: cboData,

			//editable: fileEditCheck,
			editable: false,
			cellDblClick: function(e, cell) {
				if (!fileEditCheck(cell)) return
				cell.edit(true);
			},
			formatter: function(cell, formatterParams, onRendered) {
				if (cell.getData().otType == 'O') return 'Orig';
				else if (cell.getData().otType == 'T') return 'Trans';
			},
			validator: "required"
		}, {
			title: "Lang",
			field: "lang",
			headerSort: false,
			headerHozAlign : "center",
			hozAlign:"center",
			editor: "list",
			editorParams: {
				values: {
					"EN": "EN",
					"TC": "TC",
					"SC": "SC"
				}
			},

			editable: false,
			cellDblClick: function(e, cell) {
				if (!fileEditCheck(cell)) return
				cell.edit(true);
			},
			validator: "required",
			width: 100
		}, {
			title: "File Name",
			field: "fileName",
			headerSort: false,
			formatter: "link",
			editor: fileInput,
			formatter: function(cell, formatterParams, onRendered) {
				var hddt = new Date(Date.parse($('#datepicker').val() + " " + $('#timeentry').val().replace(/(AM|PM)/i, " $1")));

				var curdt = new Date();

				if (curdt < hddt) {
					return "To be uploaded on/after hand down date";

				}
				//console.log(cell.getRow().getData())
				if (cell.getData().fileName == "") return "No file"

				else if (cell.getRow().getData().fileName == null) return "No file"

				else if (cell.getRow().getData().fileName.indexOf('\\') >= 0)

					return cell.getValue().split('\\').pop().split('/').pop();

				else if (typeof cell.getRow().getData().judgmFileId !== "undefined")
					return '<a href="/ncns/file/download/' + cell.getData().judgmFileId + '">' + cell.getValue() + '</a>';
				//else if (typeof cell.getValue() == "") return "No file"


			}
		}],
		initialFilter: [{
			field: "fileType",
			type: "=",
			value: "J"
		}],
	})

	judgmTable.on("rowClick", function(e, row) {



	})
	//judgmTable.on("rowClick", function(e, row) {

	judgmTable.on("rowDeselected", function(e, row) {
		//$('#panel-title').hide()
	})
	judgmTable.on("rowSelected", function(row) {

		if (caseTable.getSelectedRows().length == 0)
			return;

		$('#panel-title').show()

		//console.log(row.getData())

		titleTable = new Tabulator('#table-casetitle', {
			layout: "fitColumns",
			history: true,
			selectable: 1,
			selectableCheck: function(row) {

				return (!isRevoked && !isConfirmed)
			},
			data: row.getData().judgmCaseTitles,
			rowFormatter: function(row) {

				var data = row.getData();

				console.log(caseTable.getSelectedRows()[0].getData().judgmCaseId)

				console.log("judgmCaseId: " + data.judgmCaseId)
				if (typeof data.judgmCaseTitleId == "undefined") {
					if ($('#judgmId').val() !== "")
						row.getElement().classList.add("new-row")
				}
			},
			columns: [
				/*{
					title: "Case"
					,field: "caseno"
					
					
				},
				*/
				/* {
					title: "Case",
					field: "judgmCaseId",
			    
				},
				 {
					title: "File",
					field: "judgmFileId",
			    
				},*/
				{
					title: "Title",
					field: "title",
					headerSort: false,
					editor: 'input',
					validator: ["required", "minLength:2"],
					editable: (!isRevoked && !isConfirmed)
					/*
					cellDblClick: function(e, cell) {
						if (!editEnable()) return
								cell.edit(true);
					}*/
				},
			]

			/*,initialFilter: [
				[{
					field: "caseno",
					type: "=",
					value: caseTable.getSelectedRows()[0].getData().tmp
						 },
						 {
					field: "judgmCaseId",
					type: "=",
					value: caseTable.getSelectedRows()[0].getData().judgmCaseId
						 }]
					 ],
					 */


		})
		titleTable.on("rowAdded", function(row) {
			if (typeof judgmTable.getSelectedRows()[0].getData().judgmCaseTitles == "undefined")
				judgmTable.getSelectedRows()[0].getData().judgmCaseTitles = new Array()
			judgmCaseId = caseTable.getSelectedRows()[0].getData().judgmCaseId
			judgmFileId = judgmTable.getSelectedRows()[0].getData().judgmFileId
			row.getData().judgmCaseId = typeof judgmCaseId == "undefined" ? "" : judgmCaseId
			row.getData().judgmFileId = typeof judgmFileId == "undefined" ? "" : judgmFileId

			//if (typeof judgmCaseId == "undefined")
			row.getData().caseno = caseTable.getSelectedRows()[0].getCells()[0].getData().caseCourtSys
				+ caseTable.getSelectedRows()[0].getCells()[0].getData().caseType
				+ caseTable.getSelectedRows()[0].getCells()[0].getData().caseSerNo
				+ "/" + caseTable.getSelectedRows()[0].getCells()[0].getData().caseYr;

			titleCaseFound = false

			/*for (cont t of judgmTitlesAdd)
			{
				if (t.caseno==row.getData().caseno)
				{
					titleCaseFound = false
				}
			}*/

			judgmTable.getSelectedRows()[0].getData().judgmCaseTitles.push(row.getData())
			if (typeof judgmTable.getSelectedRows()[0].getData().judgmFileId !== "undefined")
				judgmTitlesAdd.push(row.getData());
		});
		titleTable.on("cellEdited", function(cell) {
			if (typeof cell.getRow().getData().judgmCaseTitleId == "undefined")
				return;
			console.log("cellEdited")
			judgmTitlesUpd = new Array()
			for (var c in titleTable.getEditedCells()) {
				if (titleTable.getEditedCells()[c].getInitialValue() !== titleTable.getEditedCells()[c].getValue())
					judgmTitlesUpd.push(titleTable.getEditedCells()[c].getData())
			}
			//judgmTitlesUpd.push(cell.getRow().getData());   
		});

		titleTable.on("tableBuilt", function(data) {
			// $('#add-titlerow').trigger('click');
			if (typeof caseTable.getSelectedRows()[0].getData().judgmCaseId == "undefined")
				titleTable.setFilter("caseno", "=", caseTable.getSelectedRows()[0].getData().tmp);
			else
				titleTable.setFilter("judgmCaseId", "=", caseTable.getSelectedRows()[0].getData().judgmCaseId);
		});


		row.update({
			titleTable: titleTable
		});



		this.redraw()

		setTimeout(function() { $('#add-titlerow').trigger('click'); }, 130);
	})



	pressTable = new Tabulator('#table-pressfile', {
		maxHeight: "100%",
		layout: "fitDataStretch",
		selectable: 1,
		selectableCheck: function(row) {

			return (!isRevoked && !isConfirmed)
		},
		validationMode: "blocking",
		history: true,
		columns: [{
			title: "Orig/Trans",
			field: "otType",
			headerSort: false,
			editor: comboEditor2,
			editorParams: cboData,
			editable: fileEditCheck,
			formatter: function(cell, formatterParams, onRendered) {
				if (cell.getData().otType == 'O') return 'Orig';
				else if (cell.getData().otType == 'T') return 'Trans';
			},
			validator: "required"

		}, {
			title: "Lang",
			field: "lang",
			headerSort: false,
			headerHozAlign : "center",
			hozAlign:"center",
			editor: "list",
			editorParams: {
				values: {
					"EN": "EN",
					"TC": "TC",
					"SC": "SC"
				}
			},
			editable: fileEditCheck,
			width: 100,
			validator: "required"
		}, {
			title: "File Name",
			field: "fileName",
			headerSort: false,
			formatter: "link",
			editor: fileInput,
			formatter: function(cell, formatterParams, onRendered) {
				console.log(cell.getData())
				//if (typeof cell.getRow().getData().judgmFileId !== "undefined") 
				//return '<a href="/ncns/file/download/' + cell.getData().judgmFileId + '">' + cell.getValue() + '</a>';
				//else if (cell.getData().fileName == "") return "No file"
				//else return cell.getValue().split('\\').pop().split('/').pop();

				if (cell.getData().fileName == "") return "No file"

				else if (cell.getRow().getData().fileName == null) return "No file"

				else if (cell.getRow().getData().fileName.indexOf('\\') >= 0)

					return cell.getValue().split('\\').pop().split('/').pop();

				else if (typeof cell.getRow().getData().judgmFileId !== "undefined")
					return '<a href="/ncns/file/download/' + cell.getData().judgmFileId + '">' + cell.getValue() + '</a>';
				//else if (typeof cell.getValue() == "") return "No file"
			}
		}],
		initialFilter: [{
			field: "fileType",
			type: "=",
			value: "P"
		}],
	})
	judgmTable.on("rowDeleted", function(row) {
		if (row.getData().judgmFileId != null)
			judgmFilesDel.push(row.getData());
	});
	pressTable.on("rowDeleted", function(row) {
		if (row.getData().judgmFileId != null)
			judgmFilesDel.push(row.getData());
	});


	appealTable = new Tabulator('#table-judgmappeal', {
		maxHeight: "100%",
		layout: "fitDataStretch",
		selectable: 1,
		selectableCheck: function(row) {

			return (!isRevoked && !isConfirmed)
		},
		//index:"judgmId",
		//data:row.getData().judgmFiles,
		columns: [{
			title: "NCN",
			field: "judgmId",
			headerSort: false,
			formatter: function(cell, formatterParams, onRendered) {

				ncnCrt = cell.getRow().getData().judgmAplFr.ncnCrt
				ncnYr = cell.getRow().getData().judgmAplFr.ncnYr
				ncnNo = cell.getRow().getData().judgmAplFr.ncnNo

				return `[${ncnYr}] ${ncnCrt} ${ncnNo}`;

			}
			, width: 150

		},
		{
			title: "Hand Down Date",
			field: "judgmAplFr.handDownDate",
			sorter: "string",
			formatter: function(cell, formatterParams, onRendered) {

				return ncnDateConvert(new Date(cell.getData().judgmAplFr.handDownDate))


			}, width: 200

		},
		{
			title: "Case No(s)", field: "caseno", headerSort: false
			, formatter: function(cell, formatterParams, onRendered) {



				cases = ''

				for (const case0 of cell.getRow().getData().judgmAplFr.judgmCaseAplFr) {
					isPrimary = case0.primary;

					c = (isPrimary == "Y" ? "<B>" : "") + case0.caseCourtSys
						+ case0.caseType
						+ case0.caseSerNo
						+ "/" + case0.caseYr
						+ (isPrimary == "Y" ? "</B>" : "")
						+ "&nbsp;&nbsp;&nbsp;"

					cases += c



				}
				return cases




			}
		},
		]
	})

	appealTable.on("rowDeleted", function(row) {
		if (row.getData().judgmAplId != null)
			judgmAplsDel.push(row.getData());

	});

	appealSearchNcnRlt = new Tabulator('#table-searchno', {
		maxHeight: "100%",
		//index: "judgmJjoId",
		layout: "fitDataStretch",
		selectable: 1,
		pagination: "local",
		paginationSize: 10,
		paginationCounter: "rows",
		movableColumns: false,
		selectableRollingSelection: false, // disable rolling selection
		columns: [{
			title: "NCN",
			field: "judgmId",
			headerSort: false,
			formatter: function(cell, formatterParams, onRendered) {
				ncnCrt = cell.getData().ncnCrt
				ncnYr = cell.getData().ncnYr
				ncnNo = cell.getData().ncnNo

				return `[${ncnYr}] ${ncnCrt} ${ncnNo}`;
			}
			, width: 150

		},
		{
			title: "Hand Down Date",
			field: "handDownDate",
			sorter: "string",
			formatter: function(cell, formatterParams, onRendered) {
				return ncnDateConvert(new Date(cell.getValue()))

			}, width: 200

		},
		{
			title: "Case No(s)", field: "caseno", headerSort: false, formatter: function(cell, formatterParams, onRendered) {
				cases = ''

				for (const case0 of cell.getRow().getData().judgmCaseAplFr) {
					isPrimary = case0.primary;

					c = (isPrimary == "Y" ? "<B>" : "") + case0.caseCourtSys
						+ case0.caseType
						+ case0.caseSerNo
						+ "/" + case0.caseYr
						+ (isPrimary == "Y" ? "</B>" : "")
						+ "&nbsp;&nbsp;&nbsp;"

					cases += c



				}
				return cases
			}
		},

		]
	})


	$('#findncn').on("click", function() {


		if ($('#radioSearchAppealNcn').is(':checked')) {

			ncnyear = $('#sltSearchAppealNcnYr').val()
			ncncrt = $('#sltSearchAppealNcnCourtPrefix').val()
			ncntseqno = $('#inputSearchAppealNcnSerno').val()

			if (ncntseqno.trim() == '') {
				$('#modal-appeal .invalid-feedback.ncnseqno').show()
				return;
			}

			ncnNo = '[' + ncnyear + '] ' + ncncrt + ' ' +
				ncntseqno

			document.getElementById("overlay2").style.display = "flex";
			var apiurl = "/ncns/ncnByNcn/apl/" + ncnyear + '-' + ncncrt + '-' + ncntseqno;

			$.ajax({
				url: apiurl,
				type: "GET",
				dataType: "json",
				contentType: 'application/json',
				crossDomain: true
			})
				.done(
					function(json) {

						if (json.data.judgm == "") {
							displayError(json.message);
							return;
						}


						appealSearchNcnRlt.setData(json.data.judgms)


					})
				.fail()
				.always(function(xhr, status) {
					document.getElementById("overlay2").style.display = "none";

				});


		}
		else if ($('#radioSearchAppealCase').is(':checked')) {

			if ($('#inputSearchAppealCase').val().trim() == '') {

				return;
			}
			document.getElementById("overlay2").style.display = "flex";

			parsedcaseno = parseCaseNo($('#inputSearchAppealCase').val().trim().toUpperCase())
			if (parsedcaseno == null) {
				displayError('Invalid Case No')
				$('#InputResCaseNo').focus()
				return;
			}
			courtSys = parsedcaseno[1];
			caseType = parsedcaseno[2];
			serNo = parsedcaseno[3];
			year = parsedcaseno[4];


			$.ajax({
				// The URL for the request
				url: "/ncns/ncnByCase/apl/" + courtSys + "/" + caseType + "/" + serNo + "/" + year,
				type: "GET",
				dataType: "json",
				contentType: 'application/json',
				crossDomain: true
			})
				.done(
					function(judgms) {

						appealSearchNcnRlt.setData(judgms)

					})
				.fail(function(xhr, status, errorThrown) {

				})
				.always(function(xhr, status) {

					document.getElementById("overlay2").style.display = "none";
				});


		}

		return
	})

	$('#find').on("click", function() {



		console.log('#find click')
		judgmCasesDel = new Array()
		judgmFilesDel = new Array()
		judgmFilesAdd = new Array()
		judgmFilesUpd = new Array()
		judgmTitlesDel = new Array()
		judgmTitlesAdd = new Array()
		judgmTitlesUpd = new Array()
		judgmJjosDel = new Array()
		judgmAplsDel = new Array()
		//judgmJjosAdd = new Array()

		funcId = $('#funcId').val()
		fileRefs = new Array()
		$('#ncn-detail').hide()
		$('#find-result').hide()
		//validation
		// find by NCN
		if ($('#flexRadioResNcn').is(':checked')) {

			if ($('#inputGroupSelect01').val().trim() == '') {
				$('.invalid-feedback.ncnyear').show()
				return;
			} else if ($('#inputGroupSelect02').val().trim() == '') {
				$('.invalid-feedback.ncn').show()
				return;
			} else if ($('#inputGroupSelect03').val().trim() == '') {
				$('.invalid-feedback.ncnseqno').show()
				return;
			}
			document.getElementById("overlay").style.display = "flex";
			ncnyear = $('#inputGroupSelect01').val()
			ncncrt = $('#inputGroupSelect02').val()
			ncntseqno = $('#inputGroupSelect03').val()

			// query the ncn
			ncnNo = '[' + ncnyear + '] ' + ncncrt + ' ' +
				ncntseqno
			var apiurl = "/ncns/ncnByNcn/" + ncnyear + '/' + ncncrt + '/' + ncntseqno;
			$.ajax({
				url: apiurl,
				type: "GET",
				dataType: "json",
				contentType: 'application/json',
				crossDomain: true
			})
				.done(
					function(json) {
						if (json.data.judgm == "") {
							displayError(json.message);
							document.getElementById("overlay").style.display = "none";
							return;
						}
						$("#search-panel input[type=radio]").prop('disabled', true);
						$("#search-panel select").prop('disabled', true);
						$("#search-panel input[type=text]").prop('readonly', true);
						$("#search-panel button").prop('disabled', true);
						$('#print-btn').show()
						$('#copy-btn').show()

						populateNcnForm(json)
					})
				.fail(function(xhr, status, errorThrown) {

					console.log(xhr)
					displayError(
						" Status: " + status
						+ "<br /> " + xhr.responseText
					)
					document.getElementById("overlay").style.display = "none";
				})
				.always(function(xhr, status) {


				});
		}
		// find by Case No
		else if ($('#flexRadioResCaseNo').is(':checked')) {
			if ($('#InputResCaseNo').val().trim() == '') {
				$('.invalid-feedback.caseno').show()
				return;
			}
			parsedcaseno = parseCaseNo($('#InputResCaseNo').val().trim().toUpperCase())
			if (parsedcaseno == null) {
				displayError('Invalid Case No')
				$('#InputResCaseNo').focus()
				return;
			}
			courtSys = parsedcaseno[1];
			caseType = parsedcaseno[2];
			serNo = parsedcaseno[3];
			year = parsedcaseno[4];
			$.ajax({
				// The URL for the request
				url: "/ncns/ncnByCase/" + courtSys + "/" + caseType + "/" + serNo + "/" + year,
				//data: JSON.stringify({"param1": courtSys + caseType ,"param2": serNo,"param3": year}), 
				type: "GET",
				dataType: "json",
				contentType: 'application/json',
				crossDomain: true
			})
				.done(
					function(json) {
						clearall()

						tableBody = $('#modal-casesearch tbody');
						json
							.forEach(function(el, i) {
								statusTxt = el.status == 'A' ? "Outstanding" : el.status;

								statusTxt = el.status == 'I' ? "Revoked" : statusTxt;

								jjos = "";
								for (const jjo of el.judgmJjos) {
									jjos += jjo.jjoTitle + "<br />";
								}
								const dt = new Date(
									el.handDownDate);
								handDownDate = ncnDateConvert(new Date(
									el.handDownDate))
								markup = '<tr><th scope="row">' +
									'<input type="hidden" name="ncnYear" value="' + el.ncnYr + '">' +
									'<input type="hidden" name="ncnCrt" value="' + el.ncnCrt + '">' +
									'<input type="hidden" name="ncnNo" value="' + el.ncnNo + '">' +
									'[' +
									el.ncnYr +
									'] ' +
									el.ncnCrt +
									" " +
									el.ncnNo +
									'</th>' +
									'<td>' +
									handDownDate +
									'</td><td>' +
									jjos +
									'</td><td>' + statusTxt + '</td></tr>';
								tableBody
									.append(markup)
							})
						tableBody.find('tr:first')
							.remove()
						bindevent_casesearch_row()
					})
				.fail(function(xhr, status, errorThrown) {
					alert("Sorry, there was a problem!");
					console.log("Error: " + errorThrown);
					console.log("Status: " + status);
					console.dir(xhr);
				})
				// Code to run regardless of success or failure;
				.always(function(xhr, status) {
					//alert( "The request is complete!" );
				});
			var myModal = new bootstrap.Modal(document
				.getElementById('modal-casesearch'), {
				keyboard: false
			})
			myModal.show()
			$("#exampleModalLabelcasesearch").html(
				'Related NCN for the Case: ' +
				$("#InputResCaseNo").val().toUpperCase())

		}
	})
	$("#InputResCaseNo, #modal-caseno, #inputSearchAppealCase").inputmask({
		//regex : "(HCMA|CA[a-zA-Z]{1,2}|HC[a-zA-Z]{1,2})[0-9]{1,3}\/[1-2][0-9]{3}$", // HKCA
		regex: "^[a-zA-Z]{3,4}[0-9]{1,6}\/[1-2][0-9]{3}$",
	});
	$('#exampleModal').on('show.bs.modal', function(event) {
		var modal = $(this)
		if (caseTable.getData().length == 0) {
			modal.find('#fg-primarycase').show()
		} else {
			modal.find('#fg-primarycase').hide()
			$('#modal-selectprimarycase').val('N')
		}
	})
	$('#exampleModal').on('shown.bs.modal', function(event) {
		$('#modal-caseno').trigger('focus')
	})
	$('#btn-modal-selectncn').click(
		function() {

			for (const ncnr of appealSearchNcnRlt.getSelectedRows()) {
				duplicatedNcn = false
				for (const ar of appealTable.getRows()) {
					console.log(ar.getData().judgmAplFrJudgmId + "  " + ncnr.getData().judgmId)
					if (ar.getData().judgmAplFrJudgmId == ncnr.getData().judgmId) {
						duplicatedNcn = true
						break;
					}
				}
				if (!duplicatedNcn) {
					appealTable.addRow(

						{
							judgmAplFrJudgmId: ncnr.getData().judgmId,
							ncnCrt: ncnr.getData().ncnCrt,
							judgmAplFr: {
								//judgmAplId:""
								judgmId: ncnr.getData().judgmId
								, ncnCrt: ncnr.getData().ncnCrt
								, ncnNo: ncnr.getData().ncnNo
								, ncnYr: ncnr.getData().ncnYr
								, handDownDate: ncnr.getData().handDownDate
								, judgmCaseAplFr: ncnr.getData().judgmCaseAplFr
							}
						}

					)

					//appealTable.addRow( ncnr.getData())

				}

			};



		})


	$('#btn-modal-savecaseno')
		.click(
			function() {

				modal_caseno = $('#modal-caseno').val()
				matchResult = parseCaseNo(modal_caseno);
				modal_caseCourtSys = matchResult[1];
				modal_caseSerNo = matchResult[3]
				modal_caseType = matchResult[2]
				modal_caseYr = matchResult[4]

				modal_caseno = modal_caseCourtSys + caseType + modal_caseSerNo + "/" + modal_caseYr
				modal_caseno = modal_caseno.toUpperCase()

				for (const c0 of caseTable.getData()) {
					existing_caseno = c0.caseCourtSys + c0.caseType + c0.caseSerNo + "/" + c0.caseYr

					if (modal_caseno == existing_caseno) {
						displayError("Case No is duplicated")
						return;
					}
				}


				modal_selectprimarycase = $('#modal-selectprimarycase').val()
				if (modal_selectprimarycase == 'NO')
					modal_selectprimarycase = ''
				if ($('#table-caseno tbody tr').length > 0)
					modal_selectprimarycase = ''

				caseTable.addRow({
					tmp: modal_caseno,
					caseCourtSys: modal_caseCourtSys.toUpperCase(),
					caseType: modal_caseType.toUpperCase(),
					caseSerNo: modal_caseSerNo,
					caseYr: modal_caseYr,
					primary: modal_selectprimarycase,
					judgmCaseTitles: new Array()
				})

				$('#exampleModal').modal('hide')
			})
	// the modal controls 
	$('#modal-revokencn').on('show.bs.modal',
		function() {
			ncnno = $('#ncn-no b').html()
			$(this).find("#alert-msg2").html(
				'Revoke <b>' + ncnno + '</b> ?')
		});
	$('#modal-judgmentupload').on('shown.bs.modal',
		function() {
			$(this).find('#modal-selectorig').trigger('focus')
			var file = document.createElement('input')
			file.setAttribute("type", "file");
			file.setAttribute("name", "jfile");
			file.setAttribute("id", "jfile" + event.timeStamp.toString().replace(".", ""));
			file.setAttribute("class", "form-control");
			$(this).find('div#fileinput').append(file)
		})

	$('#modal-casesearch').on('show.bs.modal',
		function() {
			var modal = $('#modal-casesearch')
			modal.find('tbody tr').remove()
			modal.find('tbody').append('<tr><td colspan="4" style="text-align:center"><div class="spinner" style="position:relative"></div></td></tr>');
		});
	$('#modal-appeal').on('hidden.bs.modal', function() {
		appealSearchNcnRlt.clearData()
	});

	$('[id^=modal-]').on('hidden.bs.modal', function() {
		document.getElementById("overlay").style.display = "none";
	});

});

function cntJudgmLang(table, fileType) {

	langs = ['EN', 'TC', 'SC']
	fileLangCnt = new Object();
	fileLangCnt.en = 0
	fileLangCnt.tc = 0
	fileLangCnt.sc = 0


	tb = table.getData().filter(function(value) {
		return value.fileType == fileType;
	});
	s = table.getData().filter(function(value) {
		return value.otType == 'O' && value.fileType == fileType;
	});
	if (tb.length > 0 && s.length == 0) {
		displayError('Please add orignal ' + (fileType == "J" ? "Judgment" : "Press Summary") + ' file');
		return false
	}

	if (s.length > 1) {
		displayError('There is more than 1 original file')
		return false
	}

	// count lang
	for (var lang of langs) {

		lang = lang.toLowerCase()

		s = table.getData().filter(function(value) {

			return value.fileType == fileType && value.lang.toLowerCase() == lang;
		});

		if (lang == 'en') fileLangCnt.en = s.length
		else if (lang == 'tc') fileLangCnt.tc = s.length
		else if (lang == 'sc') fileLangCnt.sc = s.length
	}

	msg = ""
	if (fileLangCnt.en > 1)
		msg += '\n\rThere is more than 1 EN ' + (fileType == "J" ? "Judgment" : "Press Summary") + ' file';

	if (fileLangCnt.tc > 1)
		msg += '\n\rThere is more than 1 TC ' + (fileType == "J" ? "Judgment" : "Press Summary") + ' file';

	if (fileLangCnt.sc > 1)
		msg += '\n\rThere is more than 1 SC ' + (fileType == "J" ? "Judgment" : "Press Summary") + ' file';
	/*
	if (judgmLangCnt.en>1)
		msg += '\n\rThere is more than 1 EN Judgment file'
		
	if (judgmLangCnt.tc>1)
		msg += '\n\rThere is more than 1 TC Judgment file'
		
	if (judgmLangCnt.sc>1)
		msg += '\n\rThere is more than 1 SC Judgment file'
	
	if (pressLangCnt.en>1)
		msg += '\n\rThere is more than 1 EN Press Summary file'
		
	if (pressLangCnt.tc>1)
		msg += '\n\rThere is more than 1 TC Press Summary file'
		
	if (pressLangCnt.sc>1)
		msg += '\n\rThere is more than 1 SC Press Summary file'
	
	*/

	if (msg !== "") {
		displayError(msg);
		return false
	}
	return true;

}

/*
function validataFiles(judgmFiles)
{
	var validated = true;

	
	validated = cntJudgmLang()
	if (!validated)
		return validated;
	
	
	for (const jfile of judgmFiles)
	{

		console.log(jfile)
		
		if (jfile.fileName == '')
		{
				alert('\nPlease select ' + jfile.lang 
				+ (jfile.fileType=="J"?" Judgment file":"")
				+ (jfile.fileType=="P"?" Press Summary file":"")
				)
				validated = false
				break;
		}
	}


	return validated;
}
*/
function validateJudgm() {
	// 'validation of judgm fields'
	isValidDate = true;
	isValidDocType = true;
	isValidHrPpr = true;
	validMsg = "";
	if ($("input#datepicker").val().trim() == "" || $("input#timeentry").val().trim() == "") {
		validMsg += 'Please enter "Hand Down Date Time"'
		isValidDate = false
	}

	if (typeof $("input:radio[name=flexRadioDocType]:checked").val() == 'undefined') {
		isValidDocType = false
		//validMsg += '\n\rPlease select "Document Type"'
		validMsg += '<br />Please select "Document Type"'
	}

	if (typeof $("input:radio[name=flexRadioDocType2]:checked").val() == 'undefined') {
		isValidHrPpr = false
		validMsg += '<br />Please select "Related To""'

	}

	if (!isValidDate || !isValidDocType || !isValidHrPpr) {
		displayError(validMsg)
		return false;
	}
	else return true
}
function populateNcnForm(json) {
	console.log('populateNcnForm')
	isConfirmed = false;
	isRevoked = false;
	jsonData = json.data
	jsonDataJudgm = json.data.judgm
	judgmXfrId = jsonDataJudgm.judgmXfrs[0].judgmXfrId
	
	
	ncn_no = "[" + jsonDataJudgm.ncnYr + "] " + jsonDataJudgm.ncnCrt + " " + jsonDataJudgm.ncnNo

	if (jsonDataJudgm.restricted == "N")
		statusTxt = jsonDataJudgm.status == 'A' ? "Outstanding" : "";
	else statusTxt = jsonDataJudgm.status == 'A' ? "Active" : "";

	statusTxt = jsonDataJudgm.status == 'I' ? "Revoked" : statusTxt;

	$('#ncn-no').html('<b>' + ncn_no + '</b> (Status: <b>' + statusTxt + "</b>)");


	ncnUrgentOldValue = jsonDataJudgm.judgmXfrs[0].urgent;
	$('#ncnUrgent').prop('checked', (ncnUrgentOldValue == 'Y' ? true : false));


	$('#judgmId').val(jsonDataJudgm.judgmId)
	$('#timeentry').timeEntry('setTime',
		jsonDataJudgm.handDownDate.substring(11, 16))


	/*
    
	hdd = new Date(jsonDataJudgm.handDownDate.substring(0, 10));

		var curdt = new Date();
		
		if (curdt<hdd)
		{
			console.log('here')
			$( "#datepicker" ).datepicker('setStartDate',  curdt.toISOString().split('T')[0]);
			//editor.setAttribute("display", "none");
		}
		else
			{
			$( "#datepicker" ).datepicker('setStartDate',  jsonDataJudgm.handDownDate.substring(0, 10));
	    
				}
    
    
	var end = new Date(new Date().toISOString().split('T')[0]);
	end = new Date(end.setDate(end.getDate() - 1))
	const dd = [];
	let loop = new Date(hdd);
	while (loop < end) {
	
	  let newDate = loop.setDate(loop.getDate() + 1);
	  loop = new Date(newDate);
		console.log(loop.toISOString().split('T')[0])
	  dd.push(loop.toISOString().split('T')[0]);

	}     
	$( "#datepicker" ).datepicker('setDatesDisabled', dd)
	*/

	$('#datepicker').datepicker("setDate",
		jsonDataJudgm.handDownDate.substring(0, 10))




	$('#ncnYr').val(jsonDataJudgm.ncnYr)
	$('#ncnCrt').val(jsonDataJudgm.ncnCrt)
	$('#ncnNo').val(jsonDataJudgm.ncnNo)
	$('#createBy').val(jsonDataJudgm.createBy)
	$('#status').val(jsonDataJudgm.status)
	isRevoked = (jsonDataJudgm.status == "I" ? true : false)
	$("input:radio[name=flexRadioDocType][value=" +
		jsonDataJudgm.docType + "]").prop('checked', true);
	$("input:radio[name=flexRadioDocType2][value=" +
		jsonDataJudgm.hrngOrPaper + "]").prop('checked', true);

	//console.log(json.data.judgm.judgmFiles.length);

	caseTable.setData(json.data.judgm.judgmCases)
	jjoTable.setData(json.data.judgm.judgmJjos)
	judgmTable.setData(json.data.judgm.judgmXfrs[0].judgmFiles)
	pressTable.setData(json.data.judgm.judgmXfrs[0].judgmFiles)
	//judgmTable.setData(json.data.judgm.judgmFiles)
	//pressTable.setData(json.data.judgm.judgmFiles)
	appealTable.setData(json.data.judgm.judgmApls)

	// Determine if isConfirmed
	jsonDataJudgm.judgmXfrs.forEach(function(el_xfr, i) {
		if (el_xfr.confirmDate != null) {
			console.log('Judgm is confirmed on ' + el_xfr.confirmDate)
			isConfirmed = true;
			$('#confirm-msg').html('<div style="padding:3px 8px;display:inline-block;border-radius: 3px;color: #0d6efd;font-weight: bold;border: 1px solid #0d6efd;">Confirmed by ' +
				"<u>" + el_xfr.confirmBy + "</u> on <u>" +
				ncnDateConvert(new Date(el_xfr.confirmDate)) +
				"</u></div>")
			return false;
		}
	})
	if (isConfirmed || isRevoked) {
		$('div#find-result input').prop("disabled", true);

		$('div.ncn-title-control > button').hide()
		$('div.ncn-title button').hide()
		$('div#find-result input').prop("disabled", true);
		$('#savencn').hide()
		$('#btn-revokencn').hide()
		$('#btn-confirmncn').hide()
		$('#btn-canceledit').hide()


	} else $('#confirm-msg').html('')


	setTimeout(selectrow, 500);
	setTimeout(showResult, 400);
	// Click the first case row


	//caseTable.getRows()[0].select()
	//caseTable.getSelectedRows()[0].getData().titleTable.
	//caseTable.selectRow(1);
	//caseTable.getSelectedRows()[0].getData().titleTable.redraw

}
function selectrow() {
	$(caseTable.getRows()[0].getElement()).trigger('click')
	$(judgmTable.getRows()[0].getElement()).trigger('click')

}
function hideResult() {
	$('#find-result').hide()
	$('#InputResNcn').val('')
	$('#inputGroupSelect02').prop("selectedIndex", 0);
	$('#inputGroupSelect03').val('')
	$('#InputResNcn').val('')
	$('#inputGroupSelect01').val(new Date().getFullYear())
	$('#InputResCaseNo').val('')
	document.getElementById("overlay").style.display = "none";
}

function showResult() {
	console.log('showResult')

	$("#createncn").hide()
	if (!isConfirmed && !isRevoked) {
		$("#savencn").show()
		$('#btn-revokencn').show()
		$('#btn-confirmncn').show()
		$('#btn-canceledit').show()
		$('button[id^=del-]').prop("disabled", false);
		$('div.ncn-title-control button, div.ncn-title button').show()
		$('div#find-result input').prop("disabled", false);

		for (const f of judgmTable.getData()) {
			if (f.fileName !== null)
				$('#datepicker').prop("disabled", true);

		}
		$('#confirm-msg').html('')
	}
	$('#find-result').show(0, function() {
	});


	document.getElementById("overlay").style.display = "none";


	$('.invalid-feedback').hide()

}
function clearall() {
	try {
		caseTable.clearData()
		jjoTable.clearData()
		judgmTable.clearData()
		pressTable.clearData()
		appealTable.clearData()
		$('#ncn-no').html('<b>---</b> (<b>Status:</b> New)')
		tableCaseTitles = new Array()
		$('#judgmId').val('')
		//$('#datepicker').val('')
		$("#datepicker").datepicker('setDatesDisabled', null)
		//$( "#datepicker" ).datepicker('setStartDate', new Date());
		$("#datepicker").datepicker('setDate', null);

		$('#timeentry').val('')
		$('#exampleFormControlInput1').val('')
		$('#panel-title').hide()
		$('#btn-revokencn').hide()
		$('#btn-confirmncn').hide()
		$('#table-caseno tbody tr').remove()
		$('#table-casetitle tbody tr').remove()
		$('#table-judgmjjo tbody tr').remove()
		$('#table-judgmfile tbody tr').remove()
		$('#table-ncn tbody tr').remove()
		$('#table-pressupload tbody tr').remove()
		$('#print-btn').hide()
		$('#copy-btn').hide()

		$("input:radio[name=flexRadioDocType]:checked")[0].checked = false;
		$("input:radio[name=flexRadioDocType2]:checked")[0].checked = false;
	} catch (error) {
		console.log(error);
	}
}
function editEnable() {
	return !isConfirmed && !isRevoked
}
// validation 
var noDivide = function(cell, value) {
	//console.log("validation")
	//cell - the cell component for the edited cell
	//value - the new input value of the cell
	//parameters - the parameters passed in with the validator

	return true
}

function confirmNcn() {
	$('#find').trigger('click');
	console.log('func confirmNcn')
	var ncn = new Object();
	ncn.judgmId = $('#judgmId').val();
	$.ajax({
		url: "/ncns/update/confirm",
		data: JSON.stringify(ncn),
		type: "POST",
		dataType: "json",
		//contentType: 'application/json',
		crossDomain: true
	})
		.done(function(json) {
			console.log('confirmNcn done')
			clearall()

			$('#find').trigger('click');

		}).fail(function(xhr, status, errorThrown) {
		})
		.always(function(xhr, status) {

		});
}

function revokeNcn() {
	var ncn = new Object();
	ncn.judgmId = $('#judgmId').val();
	$.ajax({
		url: "/ncns/revoke",
		//url: "http://localhost:8088/ncns/ncnBy/ncnByNcn/" + encodeURI(ncnNo.trim()),
		//data: JSON.stringify({"param1": courtSys + caseType ,"param2": serNo,"param3": year}), 
		data: JSON.stringify(ncn),
		type: "PUT",
		dataType: "json",
		contentType: 'application/json',
		crossDomain: true
	})
		.done(function(json) {
			//console.log(json.data.judgm)
			clearall()
			if (json.data.judgm == "") {
				displayError(json.message);
				document.getElementById("overlay").style.display = "none";
				return;
			}
			populateNcnForm(json)
		})
		.fail(function(xhr, status, errorThrown) {
			clearall()
			//alert( "Sorry, there was a problem!" );
			console.log(errorThrown);
			console.log("Request failed: " + status);
			console.dir(xhr.responseJSON);
			console.log('fail');
		})
		.always(function(xhr, status) {
			//document.getElementById("overlay").style.display = "none";
			//setTimeout(showResult, 300);
		});
}


// create ncnm
function createncn() {
	const ncn = new Object();




	ncn.judgmId = "";
	ncn.docType = $("input:radio[name=flexRadioDocType]:checked").val()
	inputHandDownDate = $("input#datepicker").val() + " " + $("input#timeentry").val()
	var m = moment(inputHandDownDate, "YYYY/MM/DD h:mmA");
	ncn.handDownDate = m.format('YYYY-MM-DDTHH:mm:00')
	ncn.lastOverallUpdDate = "";
	//ncn.ncn= $('#ncn-no b:first').text().replace(/_/g, '').trim();
	ncn.ncnCrt = $('#ncnCrt').val()
	ncn.ncnYr = $('#ncnYr').val()
	ncn.ncnNo = ""
	ncn.hrngOrPaper = $("input:radio[name=flexRadioDocType2]:checked").val()
	ncn.restricted = "N";

	/*var judgmCases = new Array();
	var judgmJjos = new Array();
	var judgmFiles = new Array();
	*/
	caseCnt = 0
	// Gather new Case and Title
	var casesAdd = new Array()

	for (const case0 of caseTable.getData()) {
		// get the new case
		if (typeof case0.judgmCaseId == "undefined") {
			var judgmCase0 = new Object();
			judgmCase0.judgmCaseId = (typeof case0.judgmCaseId == "undefined" ? "" : case0.judgmCaseId)
			judgmCase0.primary = case0.primary
			judgmCase0.caseCourtSys = case0.caseCourtSys
			judgmCase0.caseSerNo = case0.caseSerNo
			judgmCase0.caseType = case0.caseType
			judgmCase0.caseYr = case0.caseYr
			judgmCase0.ncnCrt = case0.ncnCrt
			if (typeof case0.judgmCaseTitles == "undefined")
				judgmCase0.judgmCaseTitles = new Array()
			else
				judgmCase0.judgmCaseTitles = case0.judgmCaseTitles
			casesAdd.push(judgmCase0)
		}
	}
	//Gather the edited JJos
	var judgmJjos = new Array()

	for (const editJjo of jjoTable.getEditedCells()) {
		//judgmJjo0.judgmJjoId = $(this).find("input[name=judgmJjoId]").val()
		judgmJjos.push(editJjo.getRow().getData())
	}

	judgmApls = new Array()
	for (const apl of appealTable.getData()) {

		if (typeof apl.judgmAplId == "undefined") {
			judgmApls.push(apl)

		}

	}
	var judgmFiles = new Array()
	var filesDel = new Array()
	// get new judgm file only	
	for (const judgmData of judgmTable.getData()) {
		if (typeof judgmData.judgmFileId == "undefined") {
			judgmData.fileName = judgmData.fileName.replace(/^.*[\\\/]/, '')
			delete judgmData.titleTable

			// filter logic
			var titleFiltered = new Array()
			try {
				if (judgmData.judgmCaseTitles.length > 0) {
					for (const jt of judgmData.judgmCaseTitles) {
						jt.caseno
						checkcaseno = false;
						for (const case0 of caseTable.getData()) {
							if (case0.tmp == jt.caseno)
								checkcaseno = true;
						}

						if (checkcaseno) {
							titleFiltered.push(jt)
						}

					}
				}
			} catch (exception) { }

			judgmData.judgmCaseTitles = titleFiltered;


			judgmFiles.push(judgmData)
		}
	}
	// get new press file
	for (const pressData of pressTable.getData()) {
		if (typeof pressData.judgmFileId == "undefined") {
			pressData.fileName = pressData.fileName.replace(/^.*[\\\/]/, '')
			judgmFiles.push(pressData)
		}
	}
	// 20230327
	ncn.judgmXfrs =  new Array({
		judgmXfrId : "",
		//judgmFiles: judgmFiles
	})
	
	if (judgmFiles.length > 0)
	{
		ncn.judgmXfrs[0].judgmFiles =judgmFiles
	}
	// 20230327
	
	// 'Validation of tables'
	var validated = true
	//Cases
	if (casesAdd.length == 0) {
		displayError('Please add Case')
		validated = false
	}



	if (!validateUpdateJudgmFile())
		validated = false;
	else {
		// Title
		if (!validateTitles())
			validated = false;
	}

	if (fileRefs.length > 0) {

		var hddt = new Date(Date.parse($('#datepicker').val() + " " + $('#timeentry').val().replace(/(AM|PM)/i, " $1")));
		var curdt = new Date();

		if (curdt < hddt) {
			displayError('File upload is not allowed before hand down date')
			validated = false;
		}

	}
	//Jjos
	if (validated && judgmJjos.length == 0) {
		displayError('Please add JJO')

		validated = false;
	}



	//isValidatedFiles = validataFiles(judgmFiles)
	//if (!validated || !isValidatedFiles)
	if (!validated) {
		document.getElementById("overlay").style.display = "none";
		return
	}
	// End Validation of tables'

	if (casesAdd.length > 0)
		ncn.judgmCases = casesAdd;
	if (judgmJjos.length > 0)
		ncn.judgmJjos = judgmJjos
	if (judgmApls.length > 0)
		ncn.judgmApls = judgmApls

	// 20230327
	//ncn.judgmFiles = judgmFiles
	//ncn.judgmXfrs = []
	// 20230327
	
	//prepare the form data
	let formData = new FormData()

	formData.append('test_json', JSON.stringify(ncn))
	formData.append('ncnUrgent', $('#ncnUrgent')[0].checked ? "Y" : "N")

	for (const f of fileRefs) {
		console.log(f.customId)
		formData.append('files', f.file);
	}

	console.log(JSON.stringify(ncn))
	console.log(formData)



	$.ajax({
		url: "/ncns/create",
		data: formData,
		type: "POST",
		dataType: "json",
		//contentType: 'multipart/form-data; charset=UTF-8',
		contentType: false,
		processData: false,
		crossDomain: true
	})
		.done(
			function(json) {
				$('#ncn-no b:first').text(json.ncn);
				ncn_no = "[" + json.ncnYr + "] " + json.ncnCrt + " " + json.ncnNo
				//new_ncnno = new_ncnno.replace('__','')+' '+ncn_seqno
				$("#alert-msg1").html(
					'New NCN: <b>' + ncn_no +
					'</b> is created  successfully')
				$('#ncn-no b').first().html(ncn_no)

				document.getElementById("overlay").style.display = "flex";
				setTimeout(hideResult, 300);
				var myModal = new bootstrap.Modal(document
					.getElementById('modal-createdncn'), {
					keyboard: false
				})
				document.getElementById('modal-createdncn').addEventListener('hidden.bs.modal', function() {
					$("#search-panel input[type=radio]").prop('disabled', false);
					$("#search-panel select").prop('disabled', false);
					$("#search-panel input[type=text]").prop('readonly', false);
					$("#search-panel button").prop('disabled', false);
				})
				myModal.show()

			})
		.fail(function(xhr, status, errorThrown) {

			divmsg = document.createElement('div')
			divmsg.innerHtml = '<p>' + xhr.responseText + '</p>';
			$('#exceptionMsgModal .modal-body').html('<p>' + xhr.responseText + '</p>');
			var myModal = new bootstrap.Modal('#exceptionMsgModal', {
				keyboard: false
			})

			myModal.show()
			document.getElementById("overlay").style.display = "none";
		})
		// Code to run regardless of success or failure;
		.always(function(xhr, status) {
			//alert( "The request is complete!" );
		});
}
// update ncn
function savencn(toConfirm) {
	new_ncnno = $('#ncn-no b').html()
	$("#modal-savencn #alert-msg2").html(
		'NCN: <b>' + new_ncnno +
		'</b> is updated successfully')

	var myModal = new bootstrap.Modal(document.getElementById('modal-savencn'), {
		keyboard: false
	})

	document.getElementById('modal-savencn').addEventListener('hidden.bs.modal', function() {
		$("#search-panel input[type=radio]").prop('disabled', false);
		$("#search-panel select").prop('disabled', false);
		$("#search-panel input[type=text]").prop('readonly', false);
		$("#search-panel button").prop('disabled', false);
	})
	///////update ncn ////////
	const ncn = new Object();
	ncn.judgmId = $("input#judgmId").val()
	ncn.docType = $("input:radio[name=flexRadioDocType]:checked").val()
	inputHandDownDate = $("input#datepicker").val() + " " + $("input#timeentry").val()
	var m = moment(inputHandDownDate, "YYYY/MM/DD h:mmA");
	//ncn.judgmDate = m.format('YYYY-MM-DDTHH:mm:00.000+08:00')   2022-09-20T11:30:00
	ncn.handDownDate = m.format('YYYY-MM-DDTHH:mm:00')
	ncn.lastOverallUpdDate = "";
	ncn.ncnCrt = $('#ncnCrt').val()
	ncn.ncnNo = $('#ncnNo').val()
	ncn.ncnYr = $('#ncnYr').val()
	//ncn.createBy= $('#createBy').val()
	ncn.hrngOrPaper = $("input:radio[name=flexRadioDocType2]:checked").val()
	ncn.restricted = "N";


	// Gather new Case and Title
	var casesAdd = new Array()
	var casesDel = new Array()
	for (const case0 of caseTable.getData()) {
		// get the new case
		if (typeof case0.judgmCaseId == "undefined") {
			var judgmCase0 = new Object();
			judgmCase0.judgmCaseId = (typeof case0.judgmCaseId == "undefined" ? "" : case0.judgmCaseId)
			judgmCase0.primary = case0.primary
			judgmCase0.caseCourtSys = case0.caseCourtSys
			judgmCase0.caseSerNo = case0.caseSerNo
			judgmCase0.caseType = case0.caseType
			judgmCase0.caseYr = case0.caseYr
			judgmCase0.ncnCrt = case0.ncnCrt
			if (typeof case0.judgmCaseTitles == "undefined")
				judgmCase0.judgmCaseTitles = new Array()
			else
				judgmCase0.judgmCaseTitles = case0.judgmCaseTitles
			casesAdd.push(judgmCase0)
		}
	}
	// Gather the case to be deleted
	for (const case0 of judgmCasesDel) {
		//judgmJjo0.judgmJjoId = $(this).find("input[name=judgmJjoId]").val()
		casesDel.push(case0.judgmCaseId)
	}
	console.log(casesAdd)
	console.log(casesDel)

	// Gather the added Title for existing case
	var titlesUpdAdd = new Array()
	var titlesDel = new Array()
	for (const title of judgmTitlesAdd) {

		// filter logic
		checkcaseno = false;
		for (const case0 of caseTable.getData()) {
			if (typeof case0.judgmCaseId == "undefined") {
				checkcaseno = case0.tmp
			}
			else checkcaseno = case0.ncnCrt + case0.caseSerNo + "/" + case0.caseYr

			if (checkcaseno == title.caseno)
				checkcaseno = true;

		}
		if (checkcaseno) {
			titlesUpdAdd.push(title)
		}


		//titlesUpdAdd.push(title)
	}
	//Gather the updated Title for existing case
	for (const title of judgmTitlesUpd) {
		titlesUpdAdd.push(title)
	}
	for (const title of judgmTitlesDel) {
		titlesDel.push(title.judgmCaseTitleId)
	}
	console.log(titlesUpdAdd)
	console.log(titlesDel)

	//Gather the edited JJos
	var judgmJjos = new Array()
	var jjosDel = new Array()
	for (const editJjo of jjoTable.getEditedCells()) {
		//judgmJjo0.judgmJjoId = $(this).find("input[name=judgmJjoId]").val()
		judgmJjos.push(editJjo.getRow().getData())
	}
	// Gather the Jjos to be deleted
	for (const delJjo of judgmJjosDel) {
		//judgmJjo0.judgmJjoId = $(this).find("input[name=judgmJjoId]").val()
		jjosDel.push(delJjo.judgmJjoId)
	}
	console.log(judgmJjos)
	console.log(jjosDel)


	var judgmApls = new Array()
	var aplsDel = new Array()
	for (const apl of appealTable.getData()) {
		if (typeof apl.judgmAplId == "undefined") {
			delete apl.judgmAplFr
			apl.judgmId = ncn.judgmId
			judgmApls.push(apl)
		}
	}

	// Gather the Apeal to be deleted
	for (const delApl of judgmAplsDel) {
		aplsDel.push(delApl.judgmAplId)
	}



	var judgmFiles = new Array()
	var filesDel = new Array()
	// get new judgm file only with title 	
	for (const judgmData of judgmTable.getData()) {
		console.log(judgmData)

		// new judgm
		if (typeof judgmData.judgmFileId == "undefined") {
			judgmData.fileName = judgmData.fileName.replace(/^.*[\\\/]/, '')
			delete judgmData.titleTable


			// filter logic
			var titleFiltered = new Array()
			if (judgmData.judgmCaseTitles.length > 0) {
				for (const jt of judgmData.judgmCaseTitles) {
					jt.caseno
					checkcaseno = false;
					for (const case0 of caseTable.getRows()) {
							case0_tmp = case0.getCells()[0].getElement().innerText
						  console.log(case0_tmp)
						if (case0_tmp == jt.caseno)
							checkcaseno = true;
					}
					//for (const case0 of caseTable.getData()) {
					//	if (case0.tmp == jt.caseno)
					//		checkcaseno = true;
					//}

					if (checkcaseno) {
						titleFiltered.push(jt)
					}

				}
			}
			judgmData.judgmCaseTitles = titleFiltered;

			judgmFiles.push(judgmData)
		}
		// upload judgm for existing file record
		else if (judgmData.fileName !== null) {
			if (judgmData.fileName.indexOf('\\') >= 0) {
				judgmData.fileName = judgmData.fileName.replace(/^.*[\\\/]/, '')
				delete judgmData.titleTable
				judgmFiles.push(judgmData)

			}
		}
	}
	for (const pressData of pressTable.getData()) {
		if (typeof pressData.judgmFileId == "undefined") {
			pressData.fileName = pressData.fileName.replace(/^.*[\\\/]/, '')
			judgmFiles.push(pressData)
		}
	}
	// get the existing files to be deleted
	for (const delFile of judgmFilesDel) {
		filesDel.push(delFile.judgmFileId)
	}
 
	console.log(judgmFiles)


	// 'Validation for save only'
	var validated = true

	if (caseTable.getRows().length == 0) {

		displayError('Please add Case')
		validated = false
	}

	if (!toConfirm) {
		if (!validateUpdateJudgmFile())
			validated = false;
		else {
			if (!validateTitles())
				validated = false
		}
	}

	if (fileRefs.length > 0) {

		var hddt = new Date(Date.parse($('#datepicker').val() + " " + $('#timeentry').val().replace(/(AM|PM)/i, " $1")));
		var curdt = new Date();

		if (curdt < hddt) {
			displayError('File upload is not allowed before hand down date')
			validated = false;
		}

	}
	if (jjoTable.getRows().length == 0) {
		displayError('Please add JJO')
		validated = false;
	}


	//isValidatedFiles = validataFiles(judgmFiles)
	//if (!validated || !isValidatedFiles)
	if (!validated) {
		document.getElementById("overlay").style.display = "none";
		return
	}

	// End 'Validation of tables'


	if (casesAdd.length > 0)
		ncn.judgmCases = casesAdd;
	if (judgmJjos.length > 0)
		ncn.judgmJjos = judgmJjos
	// 20230327 Start
	//if (judgmFiles.length > 0)
	//	ncn.judgmFiles = judgmFiles
	if (judgmFiles.length > 0)
	{
		ncn.judgmXfrs =  new Array({
			judgmXfrId : judgmXfrId,
			judgmFiles: judgmFiles
		})
	}
	// 20230327 End
	if (judgmApls.length > 0)
		ncn.judgmApls = judgmApls

	console.log(JSON.stringify(ncn))
	console.log(JSON.stringify(titlesUpdAdd))
	console.log(JSON.stringify(casesDel))
	console.log(JSON.stringify(titlesDel))
	console.log(JSON.stringify(jjosDel))
	console.log(JSON.stringify(aplsDel))
	console.log(JSON.stringify(filesDel))

	//prepare the form data                
	let formData = new FormData()
	formData.append('test_json', JSON.stringify(ncn))
	formData.append('titleUpdAddJson', JSON.stringify(titlesUpdAdd))
	formData.append('casesDelJson', JSON.stringify(casesDel))
	formData.append('titlesDelJson', JSON.stringify(titlesDel))
	formData.append('jjosDelJson', JSON.stringify(jjosDel))
	formData.append('appealsDelJson', JSON.stringify(aplsDel))
	formData.append('filesDelJson', JSON.stringify(filesDel))

	var ncnUrgent = $('#ncnUrgent')[0].checked ? "Y" : "N";
	if (ncnUrgent !== ncnUrgentOldValue)
		formData.append('ncnUrgent', ncnUrgent)

	// prepare the files
	fileRefs.sort(function(a, b) {
		return (a.customId > b.customId ? 1 : -1);
	});
	for (const f of fileRefs) {
		console.log(f.customId)
		formData.append('files', f.file);
	}
	console.log(formData)

	//return;

	$.ajax({
		// The URL for the request
		url: "/ncns/update" + (toConfirm ? '/confirm' : ''),
		data: formData,
		type: "POST",
		dataType: "json",
		//	contentType: 'multipart/form-data; charset=UTF-8',
		contentType: false,
		processData: false,
		crossDomain: true
	})
		.done(function(json) {

			if (!toConfirm) {
				myModal.show()
				setTimeout(hideResult, 300);
			}
			else {
				clearall()

				$('#find').trigger('click');
			}
		})
		.fail(
			function(xhr, status, errorThrown) {

				divmsg = document.createElement('div')
				divmsg.innerHtml = '<p>' + xhr.responseText + '</p>';
				$('#exceptionMsgModal .modal-body').html('<p>' + xhr.responseText + '</p>');
				var myModal = new bootstrap.Modal('#exceptionMsgModal', {
					keyboard: false
				})
				myModal.show()
				document.getElementById("overlay").style.display = "none";
			})
		.always(function(xhr, status) { });
}
$('#btn-modal-confirmncn').on('click',
	function() {
		// save and confirm
		savencn(true)

	})

$("#createncn").click(function() {
	if (!validateJudgm())
		return
	//document.getElementById("overlay").style.display = "flex";
	createncn()
})
$("#savencn").click(function() {
	//document.getElementById("overlay").style.display = "flex";
	savencn(false)
})          

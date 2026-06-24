package com.govautofill.utils

import com.govautofill.model.UserProfile

object JsFormFiller {

    fun buildScript(profile: UserProfile): String {
        val p = profile
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

        val careOfVal     = p.careOf.ifBlank { p.fatherNameEn.ifBlank { p.fatherNameBn } }
        val permCareOfVal = p.permanentCareOf.ifBlank { careOfVal }

        return """
(function() {

  // ── Profile values ────────────────────────────────────────────────────────
  var fullNameEn    = "${esc(p.fullNameEn)}";
  var fullNameBn    = "${esc(p.fullNameBn)}";
  var fatherEn      = "${esc(p.fatherNameEn)}";
  var fatherBn      = "${esc(p.fatherNameBn)}";
  var motherEn      = "${esc(p.motherNameEn)}";
  var motherBn      = "${esc(p.motherNameBn)}";
  var spouseEn      = "${esc(p.spouseNameEn)}";
  var spouseBn      = "${esc(p.spouseNameBn)}";
  var dob           = "${esc(p.dateOfBirth)}";
  var nid           = "${esc(p.nidNo)}";
  var birthCertNo   = "${esc(p.birthCertificateNo)}";
  var passport      = "${esc(p.passportNo)}";
  var mobile        = "${esc(p.mobileNo)}";
  var emailVal      = "${esc(p.email)}";
  var religion      = "${esc(p.religion)}";
  var gender        = "${esc(p.gender)}";
  var nationality   = "${esc(p.nationality)}";
  var marital       = "${esc(p.maritalStatus)}";
  var quota         = "${esc(p.quota)}";
  var depStatus     = "${esc(p.departmentalStatus)}";
  var blood         = "${esc(p.bloodGroup)}";

  var pCareOf       = "${esc(careOfVal)}";
  var pVillage      = "${esc(p.presentVillage)}";
  var pPostOffice   = "${esc(p.presentPostOffice)}";
  var pUpazila      = "${esc(p.presentUpazila)}";
  var pDistrict     = "${esc(p.presentDistrict)}";
  var pPostCode     = "${esc(p.presentPostCode)}";

  var sCareOf       = "${esc(permCareOfVal)}";
  var sVillage      = "${esc(p.permanentVillage)}";
  var sPostOffice   = "${esc(p.permanentPostOffice)}";
  var sUpazila      = "${esc(p.permanentUpazila)}";
  var sDistrict     = "${esc(p.permanentDistrict)}";
  var sPostCode     = "${esc(p.permanentPostCode)}";

  var jscExam       = "${esc(p.jscExam)}";
  var jscBoard      = "${esc(p.jscBoard)}";
  var jscRoll       = "${esc(p.jscRollNo)}";
  var jscGpa        = "${esc(p.jscGpa)}";
  var jscResultType = "${esc(p.jscResultType)}";
  var jscYear       = "${esc(p.jscYear)}";

  var sscExam       = "${esc(p.sscExam)}";
  var sscBoard      = "${esc(p.sscBoard)}";
  var sscRoll       = "${esc(p.sscRollNo)}";
  var sscReg        = "${esc(p.sscRegNo)}";
  var sscYear       = "${esc(p.sscYear)}";
  var sscGpa        = "${esc(p.sscGpa)}";
  var sscGroup      = "${esc(p.sscGroup)}";
  var sscResultType = "${esc(p.sscResultType)}";

  var hscExam       = "${esc(p.hscExam)}";
  var hscBoard      = "${esc(p.hscBoard)}";
  var hscRoll       = "${esc(p.hscRollNo)}";
  var hscReg        = "${esc(p.hscRegNo)}";
  var hscYear       = "${esc(p.hscYear)}";
  var hscGpa        = "${esc(p.hscGpa)}";
  var hscGroup      = "${esc(p.hscGroup)}";
  var hscResultType = "${esc(p.hscResultType)}";

  var gradDegree    = "${esc(p.graduationDegree)}";
  var gradSubject   = "${esc(p.graduationSubject)}";
  var gradInst      = "${esc(p.graduationInstitution)}";
  var gradYear      = "${esc(p.graduationYear)}";
  var gradResult    = "${esc(p.graduationResult)}";
  var gradResultType= "${esc(p.graduationResultType)}";
  var gradDuration  = "${esc(p.graduationDuration)}";

  // ── Synonym groups ────────────────────────────────────────────────────────
  var SYN = [
    ['male','পুরুষ'],['female','মহিলা','নারী'],
    ['islam','muslim','ইসলাম'],['hindu','hinduism','হিন্দু'],
    ['christian','christianity','খ্রিস্টান'],['buddhist','buddhism','বৌদ্ধ'],
    ['married','বিবাহিত'],['single','unmarried','অবিবাহিত'],
    ['bangladeshi','বাংলাদেশী'],
    ['not applicable','n/a'],
    ['yes','হ্যাঁ'],['no','না'],
    ['s.s.c','ssc'],['h.s.c','hsc'],
    ['science','বিজ্ঞান'],['humanities','arts','মানবিক'],
    ['business studies','commerce','ব্যবসায়'],
    ['gpa(out of 5)','gpa (out of 5)'],['gpa(out of 4)','gpa (out of 4)'],
    ['cgpa(out of 4)','cgpa (out of 4)'],
    ['1st class','first class'],['2nd class','second class'],
    ['pass course','pass'],['honors','honour','b.a.','b.sc.']
  ];
  function synMatch(a,b){
    for(var i=0;i<SYN.length;i++){
      if(SYN[i].indexOf(a)!==-1&&SYN[i].indexOf(b)!==-1)return true;
    }
    return false;
  }

  // ── Section header detection ──────────────────────────────────────────────
  var SEC=[
    {tag:'permanent',re:/permanent.*address|স্থায়ী.*ঠিকানা/i},
    {tag:'present',  re:/present.*address|বর্তমান.*ঠিকানা/i},
    {tag:'jsc',      re:/j\.?s\.?c|jdc|junior.*equiv/i},
    {tag:'hsc',      re:/h\.?s\.?c.*equiv|hsc.*level/i},
    {tag:'ssc',      re:/s\.?s\.?c.*equiv|ssc.*level/i},
    {tag:'graduation',re:/graduation.*equiv|graduation.*level/i}
  ];
  var _secCache=null;
  function secHeaders(){
    if(_secCache)return _secCache;
    _secCache=[];
    var els=document.querySelectorAll('div,h1,h2,h3,h4,h5,legend,span,td,th,p');
    for(var i=0;i<els.length;i++){
      var t=(els[i].innerText||els[i].textContent||'').trim();
      if(!t||t.length>120)continue;
      for(var j=0;j<SEC.length;j++)
        if(SEC[j].re.test(t)){_secCache.push({el:els[i],tag:SEC[j].tag});break;}
    }
    return _secCache;
  }
  function getCtx(el){
    var h=secHeaders(),best=null;
    for(var i=0;i<h.length;i++){
      var rel=h[i].el.compareDocumentPosition(el);
      if(rel&Node.DOCUMENT_POSITION_FOLLOWING)best=h[i].tag;
    }
    return best;
  }

  // ── Get all identifiers for a field ──────────────────────────────────────
  function ids(el){
    var arr=[
      el.getAttribute('name')||'',
      el.getAttribute('id')||'',
      el.getAttribute('placeholder')||'',
      el.getAttribute('aria-label')||''
    ];
    if(el.id){
      var lb=document.querySelector('label[for="'+el.id+'"]');
      if(lb)arr.push(lb.innerText||lb.textContent||'');
    }
    var pl=el.closest('label');
    if(pl)arr.push(pl.innerText||'');
    var node=el;
    for(var d=0;d<4&&node;d++){
      var ps=node.previousElementSibling;
      if(ps&&!/^(select|input|textarea)$/i.test(ps.tagName)){
        var pt=(ps.innerText||ps.textContent||'').trim();
        if(pt&&pt.length<100)arr.push(pt);
      }
      node=node.parentElement;
    }
    return arr.filter(function(s){return s.trim()!=='';});
  }

  function joined(arr){return arr.join(' ');}

  // ── Bengali field detector ────────────────────────────────────────────────
  // A field is Bengali if its name/id ends with 'ben','_bn','bn'
  // OR its closest label contains Bengali unicode characters
  function isBn(el,idArr){
    var nameAttr=(el.getAttribute('name')||'').toLowerCase();
    var idAttr=(el.getAttribute('id')||'').toLowerCase();
    // name/id ends with ben or bn
    if(/ben$|_bn$|bn$|bangla$/i.test(nameAttr)||/ben$|_bn$|bn$|bangla$/i.test(idAttr))return true;
    // label text contains Bengali unicode (range 0980–09FF)
    var j=joined(idArr);
    return /[\u0980-\u09FF]/.test(j);
  }

  // ── SELECT option finder ──────────────────────────────────────────────────
  function findOpt(sel,val){
    if(!val)return -1;
    var t=val.trim().toLowerCase();
    var opts=sel.options;
    // exact text or value
    for(var i=0;i<opts.length;i++){
      var ot=(opts[i].text||'').trim().toLowerCase();
      var ov=(opts[i].value||'').trim().toLowerCase();
      if(ot===t||ov===t)return i;
    }
    // synonym
    for(var i=0;i<opts.length;i++){
      var ot2=(opts[i].text||'').trim().toLowerCase();
      if(ot2&&synMatch(t,ot2))return i;
    }
    // contains (skip "Select")
    for(var i=0;i<opts.length;i++){
      var ot3=(opts[i].text||'').trim().toLowerCase();
      if(!ot3||ot3==='select')continue;
      if(t.length>=3&&ot3.indexOf(t)!==-1)return i;
      if(ot3.length>=3&&t.indexOf(ot3)!==-1)return i;
    }
    return -1;
  }
  function setSelect(el,val){
    if(!val||el.disabled)return false;
    var idx=findOpt(el,val);
    if(idx<0)return false;
    try{
      var d=Object.getOwnPropertyDescriptor(HTMLSelectElement.prototype,'value');
      if(d&&d.set)d.set.call(el,el.options[idx].value);
      el.selectedIndex=idx;
      ['input','change','blur'].forEach(function(e){
        el.dispatchEvent(new Event(e,{bubbles:true}));
      });
      return true;
    }catch(e){return false;}
  }

  // ── Text input setter ─────────────────────────────────────────────────────
  function pad2(n){return ('0'+n).slice(-2);}
  function toIso(ddmmyyyy){
    var m=ddmmyyyy.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
    return m?m[3]+'-'+pad2(m[1])+'-'+pad2(m[2]):null;
  }
  function setVal(el,val){
    if(!val||el.readOnly||el.disabled)return false;
    if(el.type==='date'){var iso=toIso(val);if(!iso)return false;val=iso;}
    try{
      var proto=el.tagName==='TEXTAREA'?HTMLTextAreaElement.prototype:HTMLInputElement.prototype;
      var d=Object.getOwnPropertyDescriptor(proto,'value');
      if(d&&d.set)d.set.call(el,val); else el.value=val;
      ['input','change','blur'].forEach(function(e){
        el.dispatchEvent(new Event(e,{bubbles:true}));
      });
      return true;
    }catch(e){return false;}
  }

  // ── Deferred upazila ──────────────────────────────────────────────────────
  function waitFill(el,val,n){
    (function try_(left){
      if(el.options.length>1&&setSelect(el,val))return;
      if(left>0)setTimeout(function(){try_(left-1);},350);
    })(n);
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  MAIN LOOP
  // ═══════════════════════════════════════════════════════════════════════════
  var fields=document.querySelectorAll(
    'input[type=text],input[type=email],input[type=tel],input[type=date],'+
    'input[type=number],input:not([type]),textarea,select'
  );
  var filled=0, deferred=[];

  fields.forEach(function(el){
    var idArr=ids(el);
    var j=joined(idArr).toLowerCase();
    var bn=isBn(el,idArr);
    var ctx=getCtx(el);

    // ── SELECT fields ───────────────────────────────────────────────────────
    if(el.tagName==='SELECT'){

      // 1. Yes/No dropdowns — detect by field name/id ONLY (not label)
      var nameId=((el.getAttribute('name')||'')+(el.getAttribute('id')||'')).toLowerCase();
      if(/\bnid\b/.test(nameId)){
        if(setSelect(el,nid?'Yes':'No')){filled++;return;}
      }
      if(/\bbreg\b|birth_reg|birthreg/.test(nameId)){
        if(setSelect(el,birthCertNo?'Yes':'No')){filled++;return;}
      }
      if(/\bpassport\b/.test(nameId)&&!/number|no\b/.test(nameId)){
        if(setSelect(el,passport?'Yes':'No')){filled++;return;}
      }

      // 2. Simple dropdowns
      if(/\bnationality\b/.test(j)){if(setSelect(el,nationality)){filled++;return;}}
      if(/\breligion\b/.test(j)){if(setSelect(el,religion)){filled++;return;}}
      if(/\bgender\b|\bsex\b/.test(j)){if(setSelect(el,gender)){filled++;return;}}
      if(/marital.*status|marital_status/.test(j)){if(setSelect(el,marital)){filled++;return;}}
      if(/\bquota\b/.test(j)){if(setSelect(el,quota)){filled++;return;}}
      if(/dep.*status|dep_status/.test(j)){if(setSelect(el,depStatus)){filled++;return;}}

      // 3. District (by name attribute prefix)
      if(/present.*district|district.*present/.test(j)){
        if(setSelect(el,pDistrict)){filled++;return;}
      }
      if(/permanent.*district|district.*permanent/.test(j)){
        if(setSelect(el,sDistrict)){filled++;return;}
      }

      // 4. Section-context selects
      if(ctx){
        var isUpazila=/upazila|thana|উপজেলা/.test(j);
        var val=null;
        if(/district/.test(j))      val=ctx==='permanent'?sDistrict:pDistrict;
        else if(isUpazila)          val=ctx==='permanent'?sUpazila:pUpazila;
        else if(/exam/.test(j)){
          val=ctx==='jsc'?jscExam:ctx==='ssc'?sscExam:ctx==='hsc'?hscExam:ctx==='graduation'?gradDegree:null;
        }else if(/board/.test(j)){
          val=ctx==='jsc'?jscBoard:ctx==='ssc'?sscBoard:ctx==='hsc'?hscBoard:null;
        }else if(/result/.test(j)&&!/gpa|cgpa/.test(j)){
          val=ctx==='jsc'?jscResultType:ctx==='ssc'?sscResultType:ctx==='hsc'?hscResultType:ctx==='graduation'?gradResultType:null;
        }else if(/year|pass/.test(j)){
          val=ctx==='jsc'?jscYear:ctx==='ssc'?sscYear:ctx==='hsc'?hscYear:ctx==='graduation'?gradYear:null;
        }else if(/group|subject/.test(j)){
          val=ctx==='ssc'?sscGroup:ctx==='hsc'?hscGroup:ctx==='graduation'?gradSubject:null;
        }else if(/duration/.test(j)){
          val=ctx==='graduation'?gradDuration:null;
        }

        if(val){
          if(isUpazila){deferred.push({el:el,val:val});return;}
          if(setSelect(el,val)){filled++;return;}
        }
      }
      return;
    }

    // ── TEXT / TEXTAREA fields ──────────────────────────────────────────────

    // EMAIL — must check BEFORE mobile to avoid mobile going into email box
    // Use input type="email" OR name/id contains "email" but NOT "mobile/phone/cell"
    if(el.type==='email'||(/\bemail\b/.test(j)&&!/mobile|phone|cell/.test(j))){
      if(setVal(el,emailVal)){filled++;return;}
    }

    // NAMES — use field name/id attribute for EN/BN, NOT label text
    // Bengali fields end with 'ben','_bn','bn' in name/id
    // Website pattern: applicantname, applicantnameben, fathername, fathernameben, etc.
    var nameAttr=(el.getAttribute('name')||el.getAttribute('id')||'').toLowerCase();

    // Spouse
    if(/spouse|husband.*name|wife.*name/.test(nameAttr)){
      if(setVal(el,bn?spouseBn:spouseEn)){filled++;return;}
    }
    // Mother
    if(/mother.*name|mothername/.test(nameAttr)){
      if(setVal(el,bn?motherBn:motherEn)){filled++;return;}
    }
    // Father
    if(/father.*name|fathername/.test(nameAttr)){
      if(setVal(el,bn?fatherBn:fatherEn)){filled++;return;}
    }
    // Applicant / own name (must come after father/mother check)
    if(/applicant.*name|applicantname|^name$/.test(nameAttr)&&!/father|mother|spouse/.test(nameAttr)){
      if(setVal(el,bn?fullNameBn:fullNameEn)){filled++;return;}
    }

    // DOB
    if(/date.*birth|birth.*date|^dob$/.test(j)){
      if(setVal(el,dob)){filled++;return;}
    }

    // NID number box — only fill if NID exists
    if((/\[national id number\]|national.*id.*number|nid.*number|nid.*no\b/.test(j)||
        /nid_number|nidnumber/.test(nameAttr))&&nid){
      if(setVal(el,nid)){filled++;return;}
    }

    // Birth Reg number box — only fill if birthCertNo exists AND is different from NID
    if((/\[birth registration number\]|birth.*reg.*number|birth.*cert.*number/.test(j)||
        /breg_number|bregnumber/.test(nameAttr))&&birthCertNo&&birthCertNo!==nid){
      if(setVal(el,birthCertNo)){filled++;return;}
    }

    // Passport number
    if(/passport.*number|passport.*no\b/.test(j)&&passport){
      if(setVal(el,passport)){filled++;return;}
    }

    // Mobile — only type=tel or name/id contains mobile/phone, NOT email
    if((el.type==='tel'||/mobile|phone|cell/.test(nameAttr))&&!/email/.test(nameAttr)){
      if(/confirm|re.?enter|retype/.test(j)){
        if(setVal(el,mobile)){filled++;return;}
      }else if(/mobile|phone|cell/.test(j)){
        if(setVal(el,mobile)){filled++;return;}
      }
    }

    // Spouse Name (label-based fallback for sites not using name attr)
    if(/spouse.*name|\[spouse.*name\]/.test(j)&&!nameAttr.includes('spouse')){
      if(setVal(el,bn?spouseBn:spouseEn)){filled++;return;}
    }

    // Address fields — context required
    if(ctx==='present'||ctx==='permanent'){
      var isP=ctx==='present';
      if(/care.*of/.test(j)){if(setVal(el,isP?pCareOf:sCareOf)){filled++;return;}}
      if(/village|road|house|flat/.test(j)){if(setVal(el,isP?pVillage:sVillage)){filled++;return;}}
      if(/post.*office/.test(j)){if(setVal(el,isP?pPostOffice:sPostOffice)){filled++;return;}}
      if(/post.*code|zip/.test(j)){if(setVal(el,isP?pPostCode:sPostCode)){filled++;return;}}
    }

    // Education text fields (roll, reg, gpa, university) by context
    if(ctx){
      if(/roll/.test(j)){
        var rv=ctx==='jsc'?jscRoll:ctx==='ssc'?sscRoll:ctx==='hsc'?hscRoll:'';
        if(rv&&setVal(el,rv)){filled++;return;}
      }
      if(/reg/.test(j)&&!/religion/.test(j)){
        var rgv=ctx==='ssc'?sscReg:ctx==='hsc'?hscReg:'';
        if(rgv&&setVal(el,rgv)){filled++;return;}
      }
      if(/gpa|cgpa/.test(j)){
        var gv=ctx==='jsc'?jscGpa:ctx==='ssc'?sscGpa:ctx==='hsc'?hscGpa:ctx==='graduation'?gradResult:'';
        if(gv&&setVal(el,gv)){filled++;return;}
      }
      if(ctx==='graduation'){
        if(/university|inst/.test(j)&&gradInst){if(setVal(el,gradInst)){filled++;return;}}
        if(/cgpa|result/.test(j)&&!/type/.test(j)&&gradResult){if(setVal(el,gradResult)){filled++;return;}}
      }
    }
  });

  // Deferred upazila fills (AJAX cascade)
  deferred.forEach(function(d){waitFill(d.el,d.val,20);});

  return '✅ '+filled+' টি field পূরণ হয়েছে!';
})();
""".trimIndent()
    }
}

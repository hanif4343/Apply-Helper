package com.govautofill.utils

import com.govautofill.model.UserProfile

object JsFormFiller {

    fun buildScript(profile: UserProfile): String {
        val p = profile
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

        val nidStatus      = if (p.nidNo.isNotBlank()) "Yes" else "No"
        val bregStatus     = if (p.birthCertificateNo.isNotBlank()) "Yes" else "No"
        val passportStatus = if (p.passportNo.isNotBlank()) "Yes" else "No"
        val careOfVal      = p.careOf.ifBlank { p.fatherNameEn.ifBlank { p.fatherNameBn } }
        val permCareOfVal  = p.permanentCareOf.ifBlank { careOfVal }

        return """
(function() {

  // ── Profile Data ──────────────────────────────────────────────────────────
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
  var email         = "${esc(p.email)}";
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
  var SYN_GROUPS = [
    ['male','m','পুরুষ'],
    ['female','f','মহিলা','নারী'],
    ['islam','muslim','ইসলাম'],
    ['hindu','hinduism','হিন্দু'],
    ['christian','christianity','খ্রিস্টান'],
    ['buddhist','buddhism','বৌদ্ধ'],
    ['married','বিবাহিত'],
    ['single','unmarried','অবিবাহিত'],
    ['bangladeshi','বাংলাদেশী','bangladesh'],
    ['not applicable','none','n/a'],
    ['yes','হ্যাঁ','আছে'],
    ['no','না','নাই'],
    ['s.s.c','ssc'],['h.s.c','hsc'],
    ['honors','honour','b.a.','b.sc.','b.com.'],
    ['pass course','pass'],
    ['science','বিজ্ঞান'],
    ['humanities','arts','মানবিক'],
    ['business studies','commerce','ব্যবসায়'],
    ['gpa(out of 5)','gpa (out of 5)','gpa out of 5'],
    ['gpa(out of 4)','gpa (out of 4)','gpa out of 4'],
    ['cgpa(out of 4)','cgpa (out of 4)','cgpa out of 4'],
    ['1st class','first class'],['2nd class','second class']
  ];

  function inSameSynGroup(a,b){
    for(var g=0;g<SYN_GROUPS.length;g++){
      var grp=SYN_GROUPS[g];
      if(grp.indexOf(a)!==-1&&grp.indexOf(b)!==-1)return true;
    }
    return false;
  }

  // ── Section-context detection ─────────────────────────────────────────────
  var SECTION_PATTERNS=[
    {tag:'permanent', re:/permanent[\s\S]{0,20}address|স্থায়ী[\s\S]{0,10}ঠিকানা/i},
    {tag:'present',   re:/present[\s\S]{0,20}address|বর্তমান[\s\S]{0,10}ঠিকানা/i},
    {tag:'jsc',       re:/j\.?\s*s\.?\s*c|jdc|junior|class.*eight/i},
    {tag:'hsc',       re:/h\.?\s*s\.?\s*c[\s\S]{0,30}(equivalent|level)/i},
    {tag:'ssc',       re:/s\.?\s*s\.?\s*c[\s\S]{0,30}(equivalent|level)/i},
    {tag:'graduation',re:/graduation[\s\S]{0,30}(equivalent|level)/i}
  ];
  var sectionCache=null;
  function getSectionHeaders(){
    if(sectionCache)return sectionCache;
    var found=[];
    var all=document.querySelectorAll('div,h1,h2,h3,h4,h5,legend,span,td,th,p,label');
    for(var i=0;i<all.length;i++){
      var txt=(all[i].innerText||all[i].textContent||'').trim();
      if(!txt||txt.length>100)continue;
      for(var j=0;j<SECTION_PATTERNS.length;j++){
        if(SECTION_PATTERNS[j].re.test(txt)){found.push({el:all[i],tag:SECTION_PATTERNS[j].tag});break;}
      }
    }
    sectionCache=found;
    return found;
  }
  function getSectionContext(el){
    var headers=getSectionHeaders();
    var best=null;
    for(var i=0;i<headers.length;i++){
      var rel=headers[i].el.compareDocumentPosition(el);
      if(rel&Node.DOCUMENT_POSITION_FOLLOWING)best=headers[i].tag;
    }
    return best;
  }

  // ── Identifiers collector ─────────────────────────────────────────────────
  function getIdentifiers(el){
    var ids=[
      el.getAttribute('name')||'',
      el.getAttribute('id')||'',
      el.getAttribute('placeholder')||'',
      el.getAttribute('aria-label')||''
    ];
    if(el.id){
      var lbl=document.querySelector('label[for="'+el.id+'"]');
      if(lbl)ids.push(lbl.innerText||lbl.textContent||'');
    }
    var parentLabel=el.closest('label');
    if(parentLabel)ids.push(parentLabel.innerText||'');
    var node=el;
    for(var depth=0;depth<4&&node;depth++){
      var prev=node.previousElementSibling;
      if(prev&&prev.tagName!=='SELECT'&&prev.tagName!=='INPUT'&&prev.tagName!=='TEXTAREA'){
        var t=(prev.innerText||prev.textContent||'').trim();
        if(t&&t.length<80)ids.push(t);
      }
      node=node.parentElement;
    }
    return ids.filter(function(s){return s.trim().length>0;});
  }

  // ── Name field detection — KEY FIX ───────────────────────────────────────
  // The website uses name/id like:
  //   applicantname      → English
  //   applicantnameben   → Bengali
  //   fathername         → English
  //   fathernameben / pitarnamebn → Bengali
  // We detect Bengali fields by checking if the name/id ends with 'ben','bn','bangla'
  // OR if there is a Bengali label text alongside it.
  function isBengaliField(ids){
    var joined=ids.join(' ');
    return /ben$|_bn$|bn$|bangla|বাংলা|বাংলায়|পিতার|মাতার|আবেদনকারী/i.test(joined);
  }
  function isEnglishNameField(ids){
    var joined=ids.join(' ');
    // has "name" in it but NOT a Bengali marker
    return /name/i.test(joined) && !isBengaliField(ids);
  }

  // ── Rules (name rules rebuilt with proper EN/BN split) ───────────────────
  function matchByName(ids){
    var joined=ids.join(' ');
    var bn=isBengaliField(ids);

    // Spouse
    if(/spouse|husband|wife/i.test(joined))
      return bn ? spouseBn : spouseEn;

    // Applicant / own name
    if(/applicant.*name|applicantname|^name$|নাম/i.test(joined)&&!/father|mother|পিতা|মাতা/i.test(joined))
      return bn ? fullNameBn : fullNameEn;

    // Father
    if(/father|পিতা|pitr|পিতার/i.test(joined))
      return bn ? fatherBn : fatherEn;

    // Mother
    if(/mother|মাতা|matr|মাতার/i.test(joined))
      return bn ? motherBn : motherEn;

    // DOB
    if(/date.*birth|birth.*date|^dob$|জন্ম/i.test(joined))
      return dob;

    // IDs
    if(/national.*id.*num|nid.*num|\[national id number\]/i.test(joined)) return nid;
    if(/birth.*reg.*num|\[birth registration number\]/i.test(joined)) return birthCertNo;
    if(/passport.*num/i.test(joined)) return passport;

    // Contact
    if(/confirm.*mobile|confirm.*phone/i.test(joined)) return mobile;
    if(/mobile|phone|cell|মোবাইল/i.test(joined)) return mobile;
    if(/email/i.test(joined)) return email;

    // Address care-of
    if(/care.*of/i.test(joined)){
      var ctx=getSectionContext(ids._el||document.body);
      return /permanent/i.test(joined)||ctx==='permanent' ? sCareOf : pCareOf;
    }

    // Post code
    if(/post.*code|zip/i.test(joined)){
      var ctx2=getSectionContext(ids._el||document.body);
      return ctx2==='permanent' ? sPostCode : pPostCode;
    }

    // Post office
    if(/post.*office|পোস্ট/i.test(joined)){
      var ctx3=getSectionContext(ids._el||document.body);
      return ctx3==='permanent' ? sPostOffice : pPostOffice;
    }

    // Village/road
    if(/village|road|house|flat|গ্রাম/i.test(joined)){
      var ctx4=getSectionContext(ids._el||document.body);
      return ctx4==='permanent' ? sVillage : pVillage;
    }

    return null;
  }

  // ── SELECT option matching ────────────────────────────────────────────────
  function findOptionIndex(sel,val){
    if(!val)return -1;
    var target=val.toString().trim().toLowerCase();
    if(!target)return -1;
    var opts=sel.options;
    var i;
    // Exact
    for(i=0;i<opts.length;i++){
      var t=(opts[i].text||'').trim().toLowerCase();
      var v=(opts[i].value||'').trim().toLowerCase();
      if(t===target||v===target)return i;
    }
    // Synonym
    for(i=0;i<opts.length;i++){
      var t2=(opts[i].text||'').trim().toLowerCase();
      if(t2&&inSameSynGroup(target,t2))return i;
    }
    // Contains
    for(i=0;i<opts.length;i++){
      var t3=(opts[i].text||'').trim().toLowerCase();
      if(!t3||t3==='select'||t3.startsWith('select'))continue;
      if(target.length>=3&&t3.indexOf(target)!==-1)return i;
      if(t3.length>=3&&target.indexOf(t3)!==-1)return i;
    }
    return -1;
  }

  function setSelectVal(el,val){
    if(!val||el.disabled)return false;
    var idx=findOptionIndex(el,val);
    if(idx===-1)return false;
    try{
      var desc=Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype,'value');
      if(desc&&desc.set)desc.set.call(el,el.options[idx].value);
      el.selectedIndex=idx;
      el.dispatchEvent(new Event('input',{bubbles:true}));
      el.dispatchEvent(new Event('change',{bubbles:true}));
      el.dispatchEvent(new Event('blur',{bubbles:true}));
      return true;
    }catch(e){return false;}
  }

  // ── Text input fill ───────────────────────────────────────────────────────
  function pad2(n){n=n.toString();return n.length<2?'0'+n:n;}
  function toIsoDate(ddmmyyyy){
    var m=ddmmyyyy.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
    if(!m)return null;
    return m[3]+'-'+pad2(m[1])+'-'+pad2(m[2]);
  }
  function setVal(el,val){
    if(!val||el.readOnly||el.disabled)return false;
    if(el.type==='date'){var iso=toIsoDate(val);if(iso)val=iso;else return false;}
    try{
      var desc=Object.getOwnPropertyDescriptor(
        el.tagName==='TEXTAREA'?window.HTMLTextAreaElement.prototype:window.HTMLInputElement.prototype,'value');
      if(desc&&desc.set)desc.set.call(el,val);
      else el.value=val;
      el.dispatchEvent(new Event('input',{bubbles:true}));
      el.dispatchEvent(new Event('change',{bubbles:true}));
      el.dispatchEvent(new Event('blur',{bubbles:true}));
      return true;
    }catch(e){return false;}
  }

  // ── Section-based SELECT value resolver ──────────────────────────────────
  function sectionSelectVal(ctx,ids){
    var joined=ids.join(' ');

    // District
    if(/district|জেলা/i.test(joined))
      return ctx==='permanent'?sDistrict:pDistrict;

    // Upazila (cascading — try after district change)
    if(/upazila|thana|থানা|উপজেলা/i.test(joined))
      return ctx==='permanent'?sUpazila:pUpazila;

    if(ctx==='jsc'){
      if(/exam/i.test(joined))return jscExam;
      if(/board/i.test(joined))return jscBoard;
      if(/result.*type|result$/i.test(joined))return jscResultType;
      if(/year|pass/i.test(joined))return jscYear;
      if(/group|subject/i.test(joined))return '';
    }
    if(ctx==='ssc'){
      if(/exam/i.test(joined))return sscExam;
      if(/board/i.test(joined))return sscBoard;
      if(/result.*type|result$/i.test(joined))return sscResultType;
      if(/year|pass/i.test(joined))return sscYear;
      if(/group|subject/i.test(joined))return sscGroup;
    }
    if(ctx==='hsc'){
      if(/exam/i.test(joined))return hscExam;
      if(/board/i.test(joined))return hscBoard;
      if(/result.*type|result$/i.test(joined))return hscResultType;
      if(/year|pass/i.test(joined))return hscYear;
      if(/group|subject/i.test(joined))return hscGroup;
    }
    if(ctx==='graduation'){
      if(/exam/i.test(joined))return gradDegree;
      if(/subject|degree/i.test(joined))return gradSubject;
      if(/result.*type|result$/i.test(joined))return gradResultType;
      if(/year|pass/i.test(joined))return gradYear;
      if(/duration/i.test(joined))return gradDuration;
      if(/university|inst/i.test(joined))return gradInst;
    }
    return null;
  }

  // ── Yes/No ID-type dropdowns ──────────────────────────────────────────────
  function idTypeVal(ids){
    var joined=ids.join(' ').toLowerCase();
    if(/\bnid\b|national.*id(?!.*num)/.test(joined))
      return nid?'Yes':'No';
    if(/\bbreg\b|birth.*reg(?!.*num)/.test(joined))
      return birthCertNo?'Yes':'No';
    if(/\bpassport\b(?!.*num)/.test(joined))
      return passport?'Yes':'No';
    return null;
  }

  // ── Deferred upazila fills ────────────────────────────────────────────────
  function waitFill(el,val,tries){
    function attempt(left){
      if(el.options.length>1&&setSelectVal(el,val))return;
      if(left<=0)return;
      setTimeout(function(){attempt(left-1);},350);
    }
    attempt(tries);
  }

  // ── MAIN LOOP ─────────────────────────────────────────────────────────────
  var fields=document.querySelectorAll(
    'input[type="text"],input[type="email"],input[type="tel"],input[type="date"],' +
    'input[type="number"],input:not([type]),textarea,select'
  );

  var filled=0;
  var deferredUpazila=[];

  fields.forEach(function(el){
    var ids=getIdentifiers(el);
    ids._el=el; // attach element ref for context lookup inside matchByName

    if(el.tagName==='SELECT'){
      var joined=ids.join(' ');
      var isUpazila=/upazila|থানা|উপজেলা/i.test(joined);

      // 1. ID-type Yes/No
      var idv=idTypeVal(ids);
      if(idv){if(setSelectVal(el,idv)){filled++;return;}}

      // 2. Simple named dropdowns
      if(/^nationality$|nationality/i.test(joined)){if(setSelectVal(el,nationality)){filled++;return;}}
      if(/^religion$|religion/i.test(joined)){if(setSelectVal(el,religion)){filled++;return;}}
      if(/^gender$|gender/i.test(joined)){if(setSelectVal(el,gender)){filled++;return;}}
      if(/marital.*status|^marital/i.test(joined)){if(setSelectVal(el,marital)){filled++;return;}}
      if(/^quota$/i.test(joined)){if(setSelectVal(el,quota)){filled++;return;}}
      if(/dep.*status|dep_status/i.test(joined)){if(setSelectVal(el,depStatus)){filled++;return;}}

      // 3. Section-context
      var ctx=getSectionContext(el);
      if(ctx){
        var sv=sectionSelectVal(ctx,ids);
        if(sv){
          if(isUpazila){deferredUpazila.push({el:el,val:sv});return;}
          if(setSelectVal(el,sv)){filled++;return;}
        }
      }

      // 4. District without context (fallback present)
      if(/present.*district|district.*present/i.test(joined)){
        if(isUpazila){deferredUpazila.push({el:el,val:pUpazila});return;}
        if(setSelectVal(el,pDistrict)){filled++;return;}
      }
      if(/permanent.*district|district.*permanent/i.test(joined)){
        if(isUpazila){deferredUpazila.push({el:el,val:sUpazila});return;}
        if(setSelectVal(el,sDistrict)){filled++;return;}
      }
      return;
    }

    // Text / textarea
    var textVal=matchByName(ids);
    if(textVal&&setVal(el,textVal)){filled++;return;}

    // GPA text inputs by context
    var ctx2=getSectionContext(el);
    var joined2=ids.join(' ');
    if(ctx2&&/gpa|cgpa|grade/i.test(joined2)){
      var gv=ctx2==='jsc'?jscGpa:ctx2==='ssc'?sscGpa:ctx2==='hsc'?hscGpa:ctx2==='graduation'?gradResult:'';
      if(gv&&setVal(el,gv)){filled++;return;}
    }
    // Roll
    if(ctx2&&/roll/i.test(joined2)){
      var rv=ctx2==='jsc'?jscRoll:ctx2==='ssc'?sscRoll:ctx2==='hsc'?hscRoll:'';
      if(rv&&setVal(el,rv)){filled++;return;}
    }
    // Reg
    if(ctx2&&/reg/i.test(joined2)){
      var regv=ctx2==='ssc'?sscReg:ctx2==='hsc'?hscReg:'';
      if(regv&&setVal(el,regv)){filled++;return;}
    }
    // University
    if(ctx2==='graduation'&&/university|inst/i.test(joined2)){
      if(gradInst&&setVal(el,gradInst)){filled++;return;}
    }
    // CGPA result value
    if(ctx2==='graduation'&&/cgpa|result/i.test(joined2)&&!/type/i.test(joined2)){
      if(gradResult&&setVal(el,gradResult)){filled++;return;}
    }
  });

  // Deferred upazila
  deferredUpazila.forEach(function(item){
    waitFill(item.el,item.val,20);
  });

  return "✅ "+filled+" টি field পূরণ হয়েছে!";
})();
""".trimIndent()
    }
}

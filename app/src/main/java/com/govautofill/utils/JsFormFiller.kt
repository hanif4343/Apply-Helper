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

  // ── Synonym matching ──────────────────────────────────────────────────────
  var SYN=[
    ['male','পুরুষ'],['female','মহিলা','নারী'],
    ['islam','muslim','ইসলাম'],['hindu','hinduism','হিন্দু'],
    ['christian','christianity','খ্রিস্টান'],['buddhist','buddhism','বৌদ্ধ'],
    ['married','বিবাহিত'],['single','unmarried','অবিবাহিত'],
    ['bangladeshi','বাংলাদেশী'],['not applicable','n/a'],
    ['yes','হ্যাঁ'],['no','না'],
    ['s.s.c','ssc'],['h.s.c','hsc'],
    ['science','বিজ্ঞান'],['humanities','arts','মানবিক'],
    ['business studies','commerce','ব্যবসায়'],
    ['gpa(out of 5)','gpa (out of 5)'],['gpa(out of 4)','gpa (out of 4)'],
    ['cgpa(out of 4)','cgpa (out of 4)'],
    ['1st class','first class'],['2nd class','second class'],
    ['pass course','pass'],['honors','honour']
  ];
  function synMatch(a,b){
    for(var i=0;i<SYN.length;i++)
      if(SYN[i].indexOf(a)!==-1&&SYN[i].indexOf(b)!==-1)return true;
    return false;
  }

  // ── Section context ───────────────────────────────────────────────────────
  var SECS=[
    {tag:'permanent',re:/permanent.*address|স্থায়ী.*ঠিকানা/i},
    {tag:'present',  re:/present.*address|বর্তমান.*ঠিকানা/i},
    {tag:'jsc',      re:/j\.?s\.?c.*equiv|jdc.*equiv|junior.*equiv|jsc.*level|jdc.*level/i},
    {tag:'hsc',      re:/h\.?s\.?c.*equiv|hsc.*level/i},
    {tag:'ssc',      re:/s\.?s\.?c.*equiv|ssc.*level/i},
    {tag:'graduation',re:/graduation.*equiv|graduation.*level/i}
  ];
  var _sc=null;
  function secH(){
    if(_sc)return _sc; _sc=[];
    document.querySelectorAll('div,h1,h2,h3,h4,h5,legend,span,td,th,p').forEach(function(e){
      var t=(e.innerText||e.textContent||'').trim();
      if(!t||t.length>120)return;
      for(var i=0;i<SECS.length;i++)
        if(SECS[i].re.test(t)){_sc.push({el:e,tag:SECS[i].tag});break;}
    });
    return _sc;
  }
  function getCtx(el){
    var h=secH(),best=null;
    for(var i=0;i<h.length;i++){
      if(h[i].el.compareDocumentPosition(el)&Node.DOCUMENT_POSITION_FOLLOWING)best=h[i].tag;
    }
    return best;
  }

  // ── Collect ALL identifiers for a field ───────────────────────────────────
  // Returns object with .all (string array) and .hasBn (boolean)
  function getIds(el){
    var arr=[];
    arr.push(el.getAttribute('name')||'');
    arr.push(el.getAttribute('id')||'');
    arr.push(el.getAttribute('placeholder')||'');
    arr.push(el.getAttribute('aria-label')||'');
    // label[for]
    if(el.id){
      var lb=document.querySelector('label[for="'+el.id+'"]');
      if(lb)arr.push(lb.innerText||lb.textContent||'');
    }
    // parent label
    var pl=el.closest('label');
    if(pl)arr.push(pl.innerText||'');
    // walk up 5 levels looking at previous siblings (label/td text)
    var node=el;
    for(var d=0;d<5&&node;d++){
      var ps=node.previousElementSibling;
      if(ps&&!/^(select|input|textarea|button)$/i.test(ps.tagName)){
        var t=(ps.innerText||ps.textContent||'').trim();
        if(t&&t.length<120)arr.push(t);
      }
      node=node.parentElement;
    }
    arr=arr.filter(function(s){return s.trim()!=='';});
    var joined=arr.join(' ');
    // hasBn: true if Bengali unicode OR name/id ends with 'ben','_bn','bn','bangla'
    var nameId=((el.getAttribute('name')||'')+(el.getAttribute('id')||'')).toLowerCase();
    var hasBn=/[\u0980-\u09FF]/.test(joined)||/ben$|_bn$|bn$|bangla$/i.test(nameId);
    return {all:arr, joined:joined.toLowerCase(), hasBn:hasBn, nameId:nameId};
  }

  // ── SELECT helpers ────────────────────────────────────────────────────────
  function findOpt(sel,val){
    if(!val)return -1;
    var t=val.trim().toLowerCase();
    var opts=sel.options;
    for(var i=0;i<opts.length;i++){
      var ot=(opts[i].text||'').trim().toLowerCase();
      var ov=(opts[i].value||'').trim().toLowerCase();
      if(ot===t||ov===t)return i;
    }
    for(var i=0;i<opts.length;i++){
      var ot2=(opts[i].text||'').trim().toLowerCase();
      if(ot2&&synMatch(t,ot2))return i;
    }
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
      ['input','change','blur'].forEach(function(ev){el.dispatchEvent(new Event(ev,{bubbles:true}));});
      return true;
    }catch(e){return false;}
  }

  // ── Text input setter ─────────────────────────────────────────────────────
  function pad2(n){return ('0'+n).slice(-2);}
  function toIso(s){
    var m=s.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
    return m?m[3]+'-'+pad2(m[1])+'-'+pad2(m[2]):null;
  }
  function setVal(el,val){
    if(!val||el.readOnly||el.disabled)return false;
    if(el.type==='date'){val=toIso(val);if(!val)return false;}
    try{
      var proto=el.tagName==='TEXTAREA'?HTMLTextAreaElement.prototype:HTMLInputElement.prototype;
      var d=Object.getOwnPropertyDescriptor(proto,'value');
      if(d&&d.set)d.set.call(el,val); else el.value=val;
      ['input','change','blur'].forEach(function(ev){el.dispatchEvent(new Event(ev,{bubbles:true}));});
      return true;
    }catch(e){return false;}
  }

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
    var F=getIds(el);
    var j=F.joined;           // all identifiers joined, lowercase
    var bn=F.hasBn;           // is this a Bengali field?
    var ni=F.nameId;          // name+id concatenated, lowercase
    var ctx=getCtx(el);

    // ════════════════════════════════════════════════════════════
    //  SELECT
    // ════════════════════════════════════════════════════════════
    if(el.tagName==='SELECT'){

      // Yes/No dropdowns — match by name/id attribute ONLY
      // Website uses: name="nid", name="breg", name="passport"
      if(/\bnid\b/.test(ni)&&!/number|no\b/.test(ni)){
        if(setSelect(el,nid?'Yes':'No')){filled++;return;}
      }
      if(/\bbreg\b|birth_?reg$/.test(ni)){
        if(setSelect(el,birthCertNo?'Yes':'No')){filled++;return;}
      }
      if(/\bpassport\b/.test(ni)&&!/number|no\b/.test(ni)){
        if(setSelect(el,passport?'Yes':'No')){filled++;return;}
      }

      // Simple named dropdowns (label text match)
      if(/nationality/.test(j)){if(setSelect(el,nationality)){filled++;return;}}
      if(/religion/.test(j)){if(setSelect(el,religion)){filled++;return;}}
      if(/\bgender\b/.test(j)){if(setSelect(el,gender)){filled++;return;}}
      if(/marital.{0,10}status/.test(j)){if(setSelect(el,marital)){filled++;return;}}
      if(/\bquota\b/.test(j)){if(setSelect(el,quota)){filled++;return;}}
      if(/dep.{0,15}status/.test(j)){if(setSelect(el,depStatus)){filled++;return;}}
      if(/blood.{0,10}group/.test(j)){if(setSelect(el,blood)){filled++;return;}}

      // Address district
      if(/present.{0,10}district/.test(ni)||(/district/.test(j)&&ctx==='present')){
        if(setSelect(el,pDistrict)){filled++;return;}
      }
      if(/permanent.{0,10}district/.test(ni)||(/district/.test(j)&&ctx==='permanent')){
        if(setSelect(el,sDistrict)){filled++;return;}
      }

      // Section-context selects
      if(ctx){
        var isUpa=/upazila|thana|উপজেলা/.test(j);
        var sv=null;
        if(/\bexam\b/.test(j))
          sv=ctx==='jsc'?jscExam:ctx==='ssc'?sscExam:ctx==='hsc'?hscExam:ctx==='graduation'?gradDegree:null;
        else if(/\bboard\b/.test(j))
          sv=ctx==='jsc'?jscBoard:ctx==='ssc'?sscBoard:ctx==='hsc'?hscBoard:null;
        else if(/\bresult\b/.test(j)&&!/gpa|cgpa/.test(j))
          sv=ctx==='jsc'?jscResultType:ctx==='ssc'?sscResultType:ctx==='hsc'?hscResultType:ctx==='graduation'?gradResultType:null;
        else if(/passing.{0,5}year|\byear\b/.test(j))
          sv=ctx==='jsc'?jscYear:ctx==='ssc'?sscYear:ctx==='hsc'?hscYear:ctx==='graduation'?gradYear:null;
        else if(/group|subject/.test(j))
          sv=ctx==='ssc'?sscGroup:ctx==='hsc'?hscGroup:ctx==='graduation'?gradSubject:null;
        else if(/duration/.test(j))
          sv=ctx==='graduation'?gradDuration:null;
        else if(isUpa)
          sv=ctx==='permanent'?sUpazila:pUpazila;
        else if(/district/.test(j))
          sv=ctx==='permanent'?sDistrict:pDistrict;

        if(sv){
          if(isUpa){deferred.push({el:el,val:sv});return;}
          if(setSelect(el,sv)){filled++;return;}
        }
      }
      return;
    }

    // ════════════════════════════════════════════════════════════
    //  TEXT / TEXTAREA / EMAIL
    // ════════════════════════════════════════════════════════════

    // EMAIL — type=email OR label says "email" but NOT mobile/phone
    if(el.type==='email'||(/\bemail\b/.test(j)&&!/mobile|phone|cell/.test(j))){
      if(setVal(el,emailVal)){filled++;return;}
    }

    // MOBILE — type=tel OR label/name has mobile/phone, NOT email
    if((el.type==='tel'||/mobile|phone|cell/.test(j))&&!/\bemail\b/.test(j)){
      if(setVal(el,mobile)){filled++;return;}
    }

    // DOB
    if(/date.{0,5}birth|birth.{0,5}date|\bdob\b|জন্ম.{0,5}তারিখ/.test(j)){
      if(setVal(el,dob)){filled++;return;}
    }

    // NID number box
    // Two signals: label contains "[National ID Number]" OR type=number next to NID dropdown
    if(/national.{0,5}id.{0,10}number|\[national id number\]|nid.{0,5}no|nid.{0,5}num/.test(j)){
      if(nid&&setVal(el,nid)){filled++;return;}
    }

    // Birth Registration number box
    // Only fill if birthCertNo is set AND different from NID
    if(/birth.{0,10}reg.{0,10}num|\[birth registration number\]|breg.{0,5}num/.test(j)){
      if(birthCertNo&&birthCertNo!==nid&&setVal(el,birthCertNo)){filled++;return;}
    }

    // Passport number box
    if(/passport.{0,10}num|passport.{0,5}no\b/.test(j)){
      if(passport&&setVal(el,passport)){filled++;return;}
    }

    // ── NAMES — use BOTH label text pattern AND bn flag ──────────────────
    // Logic:
    //   Step1: identify WHICH name (applicant/father/mother/spouse) by label text
    //   Step2: pick EN or BN based on bn flag (Bengali unicode in label)
    //
    // Important: check father/mother BEFORE applicant to avoid wrong match

    var isSpouse = /spouse|husband.{0,5}name|wife.{0,5}name|\[spouse/i.test(j);
    var isMother = /mother.{0,5}name|মাতার.{0,5}নাম|মাতা|মায়ের/i.test(j);
    var isFather = !isMother&&/father.{0,5}name|পিতার.{0,5}নাম|পিতা|বাবার/i.test(j);
    var isApplicant = !isFather&&!isMother&&!isSpouse&&
                      /applicant.{0,10}name|আবেদনকারীর.{0,5}নাম|^name$/.test(j);

    if(isSpouse){if(setVal(el,bn?spouseBn:spouseEn)){filled++;return;}}
    if(isMother){if(setVal(el,bn?motherBn:motherEn)){filled++;return;}}
    if(isFather){if(setVal(el,bn?fatherBn:fatherEn)){filled++;return;}}
    if(isApplicant){if(setVal(el,bn?fullNameBn:fullNameEn)){filled++;return;}}

    // ── Address text fields ───────────────────────────────────────────────
    var isP = ctx==='present', isS = ctx==='permanent';
    if(isP||isS){
      if(/care.{0,5}of/.test(j)){if(setVal(el,isP?pCareOf:sCareOf)){filled++;return;}}
      if(/village|road|house|flat|গ্রাম|মহল্লা/.test(j)){if(setVal(el,isP?pVillage:sVillage)){filled++;return;}}
      if(/post.{0,5}office|পোস্ট/.test(j)){if(setVal(el,isP?pPostOffice:sPostOffice)){filled++;return;}}
      if(/post.{0,5}code|zip/.test(j)){if(setVal(el,isP?pPostCode:sPostCode)){filled++;return;}}
    }

    // ── Education text fields ─────────────────────────────────────────────
    if(ctx){
      if(/roll/.test(j)){
        var rv=ctx==='jsc'?jscRoll:ctx==='ssc'?sscRoll:ctx==='hsc'?hscRoll:'';
        if(rv&&setVal(el,rv)){filled++;return;}
      }
      if(/reg/.test(j)&&!/religion|register/.test(j)){
        var rgv=ctx==='ssc'?sscReg:ctx==='hsc'?hscReg:'';
        if(rgv&&setVal(el,rgv)){filled++;return;}
      }
      if(/\bgpa\b|\bcgpa\b/.test(j)){
        var gv=ctx==='jsc'?jscGpa:ctx==='ssc'?sscGpa:ctx==='hsc'?hscGpa:ctx==='graduation'?gradResult:'';
        if(gv&&setVal(el,gv)){filled++;return;}
      }
      if(ctx==='graduation'){
        if(/university|inst/.test(j)){if(gradInst&&setVal(el,gradInst)){filled++;return;}}
        if(/cgpa|result/.test(j)&&!/type/.test(j)){if(gradResult&&setVal(el,gradResult)){filled++;return;}}
      }
    }
  });

  deferred.forEach(function(d){waitFill(d.el,d.val,20);});
  return '✅ '+filled+' টি field পূরণ হয়েছে!';
})();
""".trimIndent()
    }
}

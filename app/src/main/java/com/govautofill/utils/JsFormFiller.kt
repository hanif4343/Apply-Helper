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

  var pCareOf    = "${esc(careOfVal)}";
  var pVillage   = "${esc(p.presentVillage)}";
  var pPostOff   = "${esc(p.presentPostOffice)}";
  var pUpazila   = "${esc(p.presentUpazila)}";
  var pDistrict  = "${esc(p.presentDistrict)}";
  var pPostCode  = "${esc(p.presentPostCode)}";

  var sCareOf    = "${esc(permCareOfVal)}";
  var sVillage   = "${esc(p.permanentVillage)}";
  var sPostOff   = "${esc(p.permanentPostOffice)}";
  var sUpazila   = "${esc(p.permanentUpazila)}";
  var sDistrict  = "${esc(p.permanentDistrict)}";
  var sPostCode  = "${esc(p.permanentPostCode)}";

  var jscExam = "${esc(p.jscExam)}";    var jscBoard = "${esc(p.jscBoard)}";
  var jscRoll = "${esc(p.jscRollNo)}";  var jscGpa   = "${esc(p.jscGpa)}";
  var jscRT   = "${esc(p.jscResultType)}"; var jscYear= "${esc(p.jscYear)}";

  var sscExam = "${esc(p.sscExam)}";    var sscBoard = "${esc(p.sscBoard)}";
  var sscRoll = "${esc(p.sscRollNo)}";  var sscReg   = "${esc(p.sscRegNo)}";
  var sscYear = "${esc(p.sscYear)}";    var sscGpa   = "${esc(p.sscGpa)}";
  var sscGroup= "${esc(p.sscGroup)}";   var sscRT    = "${esc(p.sscResultType)}";

  var hscExam = "${esc(p.hscExam)}";    var hscBoard = "${esc(p.hscBoard)}";
  var hscRoll = "${esc(p.hscRollNo)}";  var hscReg   = "${esc(p.hscRegNo)}";
  var hscYear = "${esc(p.hscYear)}";    var hscGpa   = "${esc(p.hscGpa)}";
  var hscGroup= "${esc(p.hscGroup)}";   var hscRT    = "${esc(p.hscResultType)}";

  var gradExam = "${esc(p.graduationDegree)}";
  var gradSubj = "${esc(p.graduationSubject)}";
  var gradInst = "${esc(p.graduationInstitution)}";
  var gradYear = "${esc(p.graduationYear)}";
  var gradGpa  = "${esc(p.graduationResult)}";
  var gradRT   = "${esc(p.graduationResultType)}";
  var gradDur  = "${esc(p.graduationDuration)}";

  // ── Synonyms ──────────────────────────────────────────────────────────────
  var SYN=[
    ['male','পুরুষ'],['female','মহিলা','নারী'],
    ['islam','muslim','ইসলাম'],['hindu','hinduism','হিন্দু'],
    ['christian','christianity','খ্রিস্টান'],['buddhist','buddhism','বৌদ্ধ'],
    ['married','বিবাহিত'],['single','unmarried','অবিবাহিত'],
    ['bangladeshi','বাংলাদেশী'],['not applicable','n/a'],
    ['yes','হ্যাঁ'],['no','না'],
    ['s.s.c','ssc'],['h.s.c','hsc'],['h.s.c.','hsc'],
    ['science','বিজ্ঞান'],['humanities','arts','মানবিক'],
    ['business studies','commerce','ব্যবসায়'],
    ['gpa(out of 5)','gpa (out of 5)'],['gpa(out of 4)','gpa (out of 4)'],
    ['cgpa(out of 4)','cgpa (out of 4)'],['cgpa(out of 5)','cgpa (out of 5)'],
    ['1st class','first class'],['2nd class','second class'],
    ['pass course','pass'],['honors','honour','b.a','b.sc'],
    ['national university','national university, bangladesh']
  ];
  function synMatch(a,b){
    for(var i=0;i<SYN.length;i++)
      if(SYN[i].indexOf(a)!==-1&&SYN[i].indexOf(b)!==-1)return true;
    return false;
  }

  // ── Section context ───────────────────────────────────────────────────────
  var SECS=[
    {tag:'permanent', re:/permanent/i},
    {tag:'present',   re:/present.*address|বর্তমান/i},
    {tag:'jsc',       re:/jsc|jdc|junior/i},
    {tag:'masters',   re:/masters?|post.?grad/i},
    {tag:'graduation',re:/graduation|graduate/i},
    {tag:'hsc',       re:/hsc|h\.s\.c|alim|higher.sec/i},
    {tag:'ssc',       re:/ssc|s\.s\.c|dakhil|secondary/i}
  ];
  var _sc=null;
  function secH(){
    if(_sc)return _sc; _sc=[];
    var els=document.querySelectorAll(
      'fieldset,legend,h1,h2,h3,h4,h5,h6,'+
      'div[class*=header],div[class*=section],div[class*=title],'+
      'div[class*=card],span[class*=header],td[class*=header]'
    );
    // Also check ALL short-text elements
    var all=document.querySelectorAll('div,span,td,th,p,label,legend');
    var combined=Array.from(els).concat(Array.from(all));
    var seen=new Set();
    combined.forEach(function(e){
      if(seen.has(e))return; seen.add(e);
      // Must be short (section header, not paragraph)
      var own=e.childNodes;
      var textOnly='';
      for(var i=0;i<own.length;i++)
        if(own[i].nodeType===3)textOnly+=own[i].textContent;
      textOnly=textOnly.trim();
      if(!textOnly||textOnly.length>80)return;
      for(var j=0;j<SECS.length;j++){
        if(SECS[j].re.test(textOnly)){
          _sc.push({el:e,tag:SECS[j].tag}); break;
        }
      }
    });
    return _sc;
  }
  function getCtx(el){
    var h=secH(),best=null;
    for(var i=0;i<h.length;i++){
      var pos=h[i].el.compareDocumentPosition(el);
      if(pos&Node.DOCUMENT_POSITION_FOLLOWING)best=h[i].tag;
    }
    return best;
  }

  // ── Get field name/id (only attributes, NOT DOM walk) ────────────────────
  function attrStr(el){
    return [
      el.getAttribute('name')||'',
      el.getAttribute('id')||''
    ].join(' ').toLowerCase().trim();
  }

  // ── Bengali field: ONLY by name/id attribute suffix ───────────────────────
  // Teletalk pattern: applicant_name (EN), applicant_name_bn / applicantnameben (BN)
  // Also: placeholder with Bengali chars is reliable
  function isBnField(el){
    var ni=attrStr(el);
    var ph=(el.getAttribute('placeholder')||'').trim();
    // name/id ends with: ben, _bn, bn, bangla, _bng
    if(/ben$|_bn$|bn$|bangla$|_bng$/i.test(ni)) return true;
    // placeholder is Bengali unicode
    if(/[\u0980-\u09FF]/.test(ph)) return true;
    return false;
  }

  // ── Direct label text for this field ONLY ────────────────────────────────
  // label[for=id] or parent <label> — NOT sibling walking
  function directLabel(el){
    var t='';
    if(el.id){
      var lb=document.querySelector('label[for="'+el.id+'"]');
      if(lb) t=(lb.innerText||lb.textContent||'').trim();
    }
    if(!t){
      var pl=el.closest('label');
      if(pl) t=(pl.innerText||pl.textContent||'').replace(el.value||'','').trim();
    }
    return t.toLowerCase();
  }

  // ── Wider identifier string (for non-name fields) ─────────────────────────
  // For dropdowns and non-name fields, we DO walk siblings to get context
  function wideId(el){
    var arr=[
      el.getAttribute('name')||'',
      el.getAttribute('id')||'',
      el.getAttribute('placeholder')||'',
      el.getAttribute('aria-label')||''
    ];
    if(el.id){
      var lb=document.querySelector('label[for="'+el.id+'"]');
      if(lb) arr.push(lb.innerText||lb.textContent||'');
    }
    var pl=el.closest('label');
    if(pl) arr.push(pl.innerText||'');
    // Walk up 5 levels — for dropdowns this is fine
    var node=el;
    for(var d=0;d<5&&node;d++){
      var ps=node.previousElementSibling;
      if(ps&&!/^(select|input|textarea|button)$/i.test(ps.tagName)){
        var t=(ps.innerText||ps.textContent||'').trim();
        if(t&&t.length<120) arr.push(t);
      }
      if(node.parentElement){
        var prev=node.parentElement.previousElementSibling;
        if(prev&&/^(td|th|div|span|li)$/i.test(prev.tagName)){
          var pt=(prev.innerText||prev.textContent||'').trim();
          if(pt&&pt.length<120) arr.push(pt);
        }
      }
      node=node.parentElement;
    }
    return arr.filter(function(s){return s.trim()!=='';}).join(' ').toLowerCase();
  }

  // ── SELECT helpers ────────────────────────────────────────────────────────
  function findOpt(sel,val){
    if(!val)return -1;
    var t=val.trim().toLowerCase();
    var opts=sel.options;
    for(var i=0;i<opts.length;i++){
      if((opts[i].text||'').trim().toLowerCase()===t)return i;
      if((opts[i].value||'').trim().toLowerCase()===t)return i;
    }
    for(var i=0;i<opts.length;i++){
      var ot=(opts[i].text||'').trim().toLowerCase();
      if(ot&&synMatch(t,ot))return i;
    }
    // starts-with (for truncated display like "GPA(out o..")
    for(var i=0;i<opts.length;i++){
      var ot2=(opts[i].text||'').trim().toLowerCase();
      if(!ot2||ot2==='select')continue;
      if(t.length>=5&&ot2.indexOf(t.substring(0,5))===0)return i;
      if(ot2.length>=5&&t.indexOf(ot2.substring(0,5))===0)return i;
    }
    // contains
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
      ['input','change','blur'].forEach(function(ev){
        el.dispatchEvent(new Event(ev,{bubbles:true}));
      });
      return true;
    }catch(e){return false;}
  }

  // ── Text setter ───────────────────────────────────────────────────────────
  function pad2(n){return('0'+n).slice(-2);}
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
      ['input','change','blur'].forEach(function(ev){
        el.dispatchEvent(new Event(ev,{bubbles:true}));
      });
      return true;
    }catch(e){return false;}
  }
  function waitFill(el,val,n){
    (function try_(l){
      if(el.options.length>1&&setSelect(el,val))return;
      if(l>0)setTimeout(function(){try_(l-1);},350);
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
    var ni  = attrStr(el);           // name+id only, lowercase
    var bn  = isBnField(el);         // Bengali field? (name/id attr + placeholder only)
    var ctx = getCtx(el);            // section context
    var j   = wideId(el);            // wide identifier string (for non-name fields)
    var dlb = directLabel(el);       // direct label only

    // ════════ SELECT ═════════════════════════════════════════════════════════
    if(el.tagName==='SELECT'){

      // Yes/No by name/id only
      if(/\bnid\b/.test(ni)&&!/number|_no$/.test(ni)){
        if(setSelect(el,nid?'Yes':'No')){filled++;return;}
      }
      if(/\bbreg\b|birth_?reg$/.test(ni)){
        if(setSelect(el,birthCertNo?'Yes':'No')){filled++;return;}
      }
      if(/\bpassport\b/.test(ni)&&!/number|_no$/.test(ni)){
        if(setSelect(el,passport?'Yes':'No')){filled++;return;}
      }

      // Simple dropdowns (wide identifier)
      if(/\bnationality\b/.test(j)){if(setSelect(el,nationality)){filled++;return;}}
      if(/\breligion\b/.test(j))   {if(setSelect(el,religion)){filled++;return;}}
      if(/\bgender\b|\bsex\b/.test(j)){if(setSelect(el,gender)){filled++;return;}}
      if(/marital/.test(j))        {if(setSelect(el,marital)){filled++;return;}}
      if(/\bquota\b/.test(j))      {if(setSelect(el,quota)){filled++;return;}}
      if(/dep.{0,15}status/.test(j)){if(setSelect(el,depStatus)){filled++;return;}}
      if(/blood.{0,10}group/.test(j)){if(setSelect(el,blood)){filled++;return;}}

      // District
      if(/present.{0,5}district/.test(ni)||(/\bdistrict\b/.test(j)&&ctx==='present')){
        if(setSelect(el,pDistrict)){filled++;return;}
      }
      if(/permanent.{0,5}district/.test(ni)||(/\bdistrict\b/.test(j)&&ctx==='permanent')){
        if(setSelect(el,sDistrict)){filled++;return;}
      }

      // Address upazila
      if((ctx==='present'||ctx==='permanent')&&/upazila|thana/.test(j)){
        deferred.push({el:el,val:ctx==='permanent'?sUpazila:pUpazila});return;
      }
      if((ctx==='present'||ctx==='permanent')&&/\bdistrict\b/.test(j)){
        if(setSelect(el,ctx==='permanent'?sDistrict:pDistrict)){filled++;return;}
      }

      // Education section selects
      if(ctx&&ctx!=='present'&&ctx!=='permanent'&&ctx!=='masters'){
        var isUpa=/upazila|thana/.test(j);
        var sv=null;
        if(/\bexam\b|examination/.test(j))
          sv=ctx==='jsc'?jscExam:ctx==='ssc'?sscExam:ctx==='hsc'?hscExam:ctx==='graduation'?gradExam:null;
        else if(/\bboard\b/.test(j))
          sv=ctx==='jsc'?jscBoard:ctx==='ssc'?sscBoard:ctx==='hsc'?hscBoard:null;
        else if(/\bresult\b/.test(j)&&!/gpa|cgpa|value|score|[0-9]/.test(j))
          sv=ctx==='jsc'?jscRT:ctx==='ssc'?sscRT:ctx==='hsc'?hscRT:ctx==='graduation'?gradRT:null;
        else if(/passing.{0,5}year|\byear\b/.test(j))
          sv=ctx==='jsc'?jscYear:ctx==='ssc'?sscYear:ctx==='hsc'?hscYear:ctx==='graduation'?gradYear:null;
        else if(/group|subject|degree/.test(j))
          sv=ctx==='ssc'?sscGroup:ctx==='hsc'?hscGroup:ctx==='graduation'?gradSubj:null;
        else if(/duration/.test(j))
          sv=ctx==='graduation'?gradDur:null;
        else if(/university|inst/.test(j))
          sv=ctx==='graduation'?gradInst:null;

        if(sv){
          if(isUpa){deferred.push({el:el,val:sv});return;}
          if(setSelect(el,sv)){filled++;return;}
        }
      }
      return;
    }

    // ════════ TEXT / EMAIL / TEL ══════════════════════════════════════════════

    // Email
    if(el.type==='email'||(/\bemail\b/.test(j)&&!/mobile|phone|cell/.test(j))){
      if(setVal(el,emailVal)){filled++;return;}
    }
    // Mobile
    if((el.type==='tel'||/mobile|phone|cell/.test(j))&&!/\bemail\b/.test(j)){
      if(setVal(el,mobile)){filled++;return;}
    }
    // DOB
    if(/date.{0,5}birth|birth.{0,5}date|\bdob\b|জন্ম/.test(j)){
      if(setVal(el,dob)){filled++;return;}
    }
    // NID number
    if(/national.{0,5}id.{0,10}num|\[national id|\bnid.{0,5}no\b|nid.{0,5}num/.test(j)){
      if(nid&&setVal(el,nid)){filled++;return;}
    }
    // Birth cert number (only if different from NID)
    if(/birth.{0,10}reg.{0,10}num|\[birth reg|breg.{0,5}num/.test(j)){
      if(birthCertNo&&birthCertNo!==nid&&setVal(el,birthCertNo)){filled++;return;}
    }
    // Passport number
    if(/passport.{0,10}num|passport.{0,5}no\b/.test(j)){
      if(passport&&setVal(el,passport)){filled++;return;}
    }

    // ── NAMES ──────────────────────────────────────────────────────────────
    // EN/BN detection: ONLY use name/id attribute suffix + placeholder
    // Do NOT use label text or DOM walk for this decision
    //
    // Which name: use ONLY the name/id attribute pattern
    // Teletalk: applicant_name, father_name, mother_name, spouse_name
    //           applicant_name_bn, father_name_bn, mother_name_bn
    // Other sites: similar patterns

    var isSpouseField   = /spouse|husband_name|wife_name/.test(ni);
    var isMotherField   = /mother_?name|mother_?nam/.test(ni);
    var isFatherField   = !isMotherField && /father_?name|father_?nam/.test(ni);
    var isApplicantField= !isFatherField&&!isMotherField&&!isSpouseField&&
                          /applicant_?name|^name$|appl.*nam/.test(ni);

    // Fallback: if name attr doesn't match, use direct label (label[for] only)
    if(!isSpouseField&&!isMotherField&&!isFatherField&&!isApplicantField){
      if(/spouse|husband.{0,5}name|wife.{0,5}name/i.test(dlb)) isSpouseField=true;
      else if(/mother.{0,5}name|মাতার/i.test(dlb))             isMotherField=true;
      else if(/father.{0,5}name|পিতার/i.test(dlb))             isFatherField=true;
      else if(/applicant.{0,5}name|আবেদনকারী/i.test(dlb))      isApplicantField=true;
    }

    if(isSpouseField   ){if(setVal(el,bn?spouseBn :spouseEn )){filled++;return;}}
    if(isMotherField   ){if(setVal(el,bn?motherBn :motherEn )){filled++;return;}}
    if(isFatherField   ){if(setVal(el,bn?fatherBn :fatherEn )){filled++;return;}}
    if(isApplicantField){if(setVal(el,bn?fullNameBn:fullNameEn)){filled++;return;}}

    // ── Address text ──────────────────────────────────────────────────────
    if(ctx==='present'||ctx==='permanent'){
      var isP=ctx==='present';
      if(/care.{0,5}of/.test(j)){if(setVal(el,isP?pCareOf:sCareOf)){filled++;return;}}
      if(/village|road|house|flat|গ্রাম|মহল্লা/.test(j)&&!/care.{0,3}of/.test(j)){
        if(setVal(el,isP?pVillage:sVillage)){filled++;return;}
      }
      if(/post.{0,5}office|পোস্ট/.test(j)){if(setVal(el,isP?pPostOff:sPostOff)){filled++;return;}}
      if(/post.{0,5}code|\bzip\b|\bcode\b/.test(j)){if(setVal(el,isP?pPostCode:sPostCode)){filled++;return;}}
    }

    // ── Education text ────────────────────────────────────────────────────
    if(ctx&&ctx!=='present'&&ctx!=='permanent'&&ctx!=='masters'){
      if(/\broll\b/.test(j)){
        var rv=ctx==='jsc'?jscRoll:ctx==='ssc'?sscRoll:ctx==='hsc'?hscRoll:'';
        if(rv&&setVal(el,rv)){filled++;return;}
      }
      if(/\breg\b|registration/.test(j)&&!/religion/.test(j)){
        var rgv=ctx==='ssc'?sscReg:ctx==='hsc'?hscReg:'';
        if(rgv&&setVal(el,rgv)){filled++;return;}
      }
      // GPA value input
      if(/\bgpa\b|\bcgpa\b/.test(j)||(el.type==='number'&&/result/.test(j))){
        var gv=ctx==='jsc'?jscGpa:ctx==='ssc'?sscGpa:ctx==='hsc'?hscGpa
              :ctx==='graduation'?gradGpa:'';
        if(gv&&setVal(el,gv)){filled++;return;}
      }
      if(ctx==='graduation'){
        if(/university|inst/.test(j)){if(gradInst&&setVal(el,gradInst)){filled++;return;}}
        if(/cgpa|result|grade/.test(j)&&!/type/.test(j)){if(gradGpa&&setVal(el,gradGpa)){filled++;return;}}
      }
    }
  });

  deferred.forEach(function(d){waitFill(d.el,d.val,20);});
  return '✅ '+filled+' টি field পূরণ হয়েছে!';
})();
""".trimIndent()
    }
}

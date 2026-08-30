package com.akslabs.circletosearch

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF080612)
private val Surface = Color(0xFF110D20)
private val Raised = Color(0xFF18112B)
private val Violet = Color(0xFF9B6CFF)
private val VioletBright = Color(0xFFC09BFF)
private val Blue = Color(0xFF657CFF)
private val Cyan = Color(0xFF58D6FF)
private val Green = Color(0xFF43E0AE)
private val Amber = Color(0xFFFFC45C)
private val Red = Color(0xFFFF5F7A)
private val TextPrimary = Color(0xFFF4F0FF)
private val TextMuted = Color(0xFFA49BB8)
private val TextDim = Color(0xFF665C79)

@Composable
fun ScamShieldOnboardingScreen(onEnable: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Background).padding(horizontal = 28.dp, vertical = 26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(126.dp).clip(RoundedCornerShape(38.dp)).background(Brush.linearGradient(listOf(Color(0xFF3A176F), Color(0xFF11102A)))).border(1.dp, Violet.copy(alpha=.55f), RoundedCornerShape(38.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Shield, null, tint = VioletBright, modifier = Modifier.size(68.dp))
        }
        Spacer(Modifier.height(30.dp))
        Text("SCAMSHIELD", color=TextPrimary,fontSize=28.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=2.sp)
        Spacer(Modifier.height(6.dp)); Text("YOUR DIGITAL SHIELD",color=VioletBright,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=2.4.sp)
        Spacer(Modifier.height(34.dp)); Text("Protect yourself while you use other apps.",color=TextPrimary,fontSize=18.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center)
        Spacer(Modifier.height(10.dp)); Text("Enable ScamShield to scan suspicious content through the system overlay.",color=TextMuted,fontSize=13.sp,lineHeight=20.sp,textAlign=TextAlign.Center)
        Spacer(Modifier.height(28.dp)); Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Security,null,tint=Green,modifier=Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Required for in-context protection",color=TextMuted,fontSize=12.sp)}
        Spacer(Modifier.height(28.dp)); PrimaryButton("ENABLE SCAMSHIELD",onEnable)
    }
}

@Composable
fun ScamShieldHomeScreen(onManualScan:()->Unit,onScreenshot:()->Unit,onCommunity:()->Unit,onSettings:()->Unit){
    Column(Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(horizontal=20.dp,vertical=18.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
            Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(Color(0xFF6E39C9),Color(0xFF1A1536)))),contentAlignment=Alignment.Center){Icon(Icons.Default.Shield,null,tint=VioletBright,modifier=Modifier.size(25.dp))};Spacer(Modifier.width(11.dp));Column{Text("SCAMSHIELD",color=TextPrimary,fontWeight=FontWeight.ExtraBold,letterSpacing=1.5.sp);Text("YOUR DIGITAL SHIELD",color=TextDim,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp)}}
            IconButton(onClick=onSettings){Icon(Icons.Default.Settings,"Settings",tint=TextMuted)}
        }
        Spacer(Modifier.height(22.dp)); ProtectionCard()
        Spacer(Modifier.height(28.dp)); SectionLabel("CHECK SOMETHING SUSPICIOUS");Spacer(Modifier.height(8.dp));Text("Paste any suspicious URL, message or text",color=TextPrimary,fontSize=21.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));PasteCard(onAnalyze=onManualScan);Spacer(Modifier.height(12.dp));UploadCard(onClick=onScreenshot)
        Spacer(Modifier.height(28.dp));SectionLabel("COMMUNITY INTELLIGENCE");Spacer(Modifier.height(10.dp));CommunityEntry(onClick=onCommunity);Spacer(Modifier.height(16.dp));ChakshuInfoCard();Spacer(Modifier.height(18.dp))
    }
}

@Composable private fun ProtectionCard(){Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Color(0xFF21123F),Color(0xFF0E1024)))).border(1.dp,Violet.copy(alpha=.42f),RoundedCornerShape(28.dp)).padding(20.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(76.dp).clip(CircleShape).background(Violet.copy(alpha=.12f)).border(1.dp,Violet.copy(alpha=.48f),CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Default.Shield,null,tint=VioletBright,modifier=Modifier.size(40.dp))};Spacer(Modifier.width(17.dp));Column(Modifier.weight(1f)){Text("PROTECTION STATUS",color=TextDim,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp);Spacer(Modifier.height(4.dp));Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(8.dp).clip(CircleShape).background(Green));Spacer(Modifier.width(7.dp));Text("SCAMSHIELD ACTIVE",color=Green,fontSize=15.sp,fontWeight=FontWeight.Bold)};Spacer(Modifier.height(5.dp));Text("In-context scanning is ready.",color=TextMuted,fontSize=11.sp)}}}}

@Composable private fun PasteCard(onAnalyze:()->Unit){var text by remember{mutableStateOf("")};Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Surface).border(1.dp,Violet.copy(alpha=.22f),RoundedCornerShape(22.dp)).padding(16.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Link,null,tint=Violet,modifier=Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("TEXT / URL",color=VioletBright,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp)};Spacer(Modifier.height(12.dp));Box(Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xFF0C0917)).padding(13.dp)){BasicTextField(value=text,onValueChange={text=it},textStyle=androidx.compose.ui.text.TextStyle(color=TextPrimary,fontSize=13.sp),modifier=Modifier.fillMaxWidth());if(text.isEmpty())Text("Paste a suspicious message or URL…",color=TextDim,fontSize=13.sp)};Spacer(Modifier.height(11.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Copied SMS, WhatsApp text, links, etc.",color=TextDim,fontSize=10.sp);Text("ANALYZE →",color=VioletBright,fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.clickable(onClick=onAnalyze))}}}

@Composable private fun UploadCard(onClick:()->Unit){Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Raised).border(1.dp,Color.White.copy(alpha=.06f),RoundedCornerShape(20.dp)).clickable(onClick=onClick).padding(17.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Violet.copy(alpha=.13f)),contentAlignment=Alignment.Center){Icon(Icons.Default.Image,null,tint=VioletBright,modifier=Modifier.size(22.dp))};Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f)){Text("SCREENSHOT / FILE",color=TextPrimary,fontWeight=FontWeight.SemiBold,fontSize=14.sp);Text("Upload something suspicious for analysis",color=TextMuted,fontSize=11.sp)};Icon(Icons.Default.Add,null,tint=VioletBright)}}

@Composable private fun CommunityEntry(onClick:()->Unit){Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFF15102A),Color(0xFF0F1222)))).border(1.dp,Color(0xFF5B438B).copy(alpha=.5f),RoundedCornerShape(22.dp)).clickable(onClick=onClick).padding(18.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(45.dp).clip(CircleShape).background(Color(0xFF7D4BDB).copy(alpha=.14f)),contentAlignment=Alignment.Center){Icon(Icons.Default.Groups,null,tint=VioletBright,modifier=Modifier.size(23.dp))};Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f)){Text("SEE RECENT SCAM REPORTS",color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=14.sp);Text("Learn what other users are seeing",color=TextMuted,fontSize=11.sp)};Icon(Icons.Default.ArrowForward,null,tint=TextMuted)}}

@Composable private fun ChakshuInfoCard(){val context=LocalContext.current;var expanded by remember{mutableStateOf(false)};Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF0D0A17)).border(1.dp,Color.White.copy(alpha=.06f),RoundedCornerShape(18.dp))){Row(Modifier.fillMaxWidth().clickable{expanded=!expanded}.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Info,null,tint=Cyan,modifier=Modifier.size(18.dp));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text("WHAT IS CHAKSHU?",color=TextPrimary,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=.8.sp);Text("Government reporting for suspected fraud communications",color=TextDim,fontSize=10.sp)};Text(if(expanded)"−" else "+",color=VioletBright,fontSize=20.sp)};if(expanded){Column(Modifier.padding(start=15.dp,end=15.dp,bottom=15.dp)){Text("Chakshu is a Government of India facility on Sanchar Saathi for reporting suspected fraudulent communications such as calls, SMS and WhatsApp messages.",color=TextMuted,fontSize=11.sp,lineHeight=17.sp);Spacer(Modifier.height(10.dp));Text("REPORT ON CHAKSHU →",color=Cyan,fontSize=10.sp,fontWeight=FontWeight.Bold,modifier=Modifier.clickable{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://sancharsaathi.gov.in/sfc/")))})}}}}

@Composable fun ManualScanScreen(onBack:()->Unit){val clipboard=LocalClipboardManager.current;var text by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().background(Background).padding(20.dp).verticalScroll(rememberScrollState())){ScreenHeader("CHECK SOMETHING",onBack);Spacer(Modifier.height(22.dp));Text("Paste anything suspicious",color=TextPrimary,fontSize=25.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(7.dp));Text("Messages, URLs or copied text can be checked here.",color=TextMuted,fontSize=12.sp);Spacer(Modifier.height(18.dp));Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(22.dp)).background(Surface).border(1.dp,Violet.copy(alpha=.24f),RoundedCornerShape(22.dp)).padding(16.dp)){BasicTextField(value=text,onValueChange={text=it},textStyle=androidx.compose.ui.text.TextStyle(color=TextPrimary,fontSize=14.sp,lineHeight=21.sp),modifier=Modifier.fillMaxWidth());if(text.isEmpty())Text("Paste a suspicious message or URL…",color=TextDim,fontSize=14.sp)};Spacer(Modifier.height(12.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){SmallButton("PASTE",Violet){clipboard.getText()?.let{text=it.text}};SmallButton("CLEAR",TextMuted){text=""}};Spacer(Modifier.height(16.dp));PrimaryButton("ANALYZE THREAT",{});Spacer(Modifier.height(16.dp));Text("-",color=TextDim,fontSize=18.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)}}

@Composable fun ScreenshotInputScreen(onBack:()->Unit){var selected by remember{mutableStateOf<Uri?>(null)};val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){selected=it};Column(Modifier.fillMaxSize().background(Background).padding(20.dp).verticalScroll(rememberScrollState())){ScreenHeader("SCREENSHOT / FILE",onBack);Spacer(Modifier.height(22.dp));Text("Upload something suspicious",color=TextPrimary,fontSize=25.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(7.dp));Text("Add a screenshot or file to check.",color=TextMuted,fontSize=12.sp,lineHeight=18.sp);Spacer(Modifier.height(20.dp));Box(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(24.dp)).background(Surface).border(1.dp,Violet.copy(alpha=.3f),RoundedCornerShape(24.dp)).clickable{picker.launch("*/*")},contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Add,null,tint=VioletBright,modifier=Modifier.size(38.dp));Spacer(Modifier.height(9.dp));Text(if(selected==null)"ADD SCREENSHOT OR FILE" else "FILE SELECTED",color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=12.sp,letterSpacing=1.sp);if(selected!=null){Spacer(Modifier.height(5.dp));Text(selected.toString().takeLast(42),color=TextDim,fontSize=10.sp)}}};Spacer(Modifier.height(16.dp));PrimaryButton("ANALYZE",{});Spacer(Modifier.height(15.dp));Text("-",color=TextDim,fontSize=18.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)}}

@Composable fun CommunityScreen(onBack:()->Unit,onReport:()->Unit){var showChakshu by remember{mutableStateOf(false)};val context=LocalContext.current;Column(Modifier.fillMaxSize().background(Background).padding(20.dp).verticalScroll(rememberScrollState())){ScreenHeader("COMMUNITY INTELLIGENCE",onBack);Spacer(Modifier.height(20.dp));Text("What scammers are trying now.",color=TextPrimary,fontSize=25.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(6.dp));Text("Recent reports help the community spot recurring patterns.",color=TextMuted,fontSize=12.sp);Spacer(Modifier.height(18.dp));CommunityReportCard("Fake delivery fee SMS","PHISHING","Community report",Amber);CommunityReportCard("UPI refund impersonation","PAYMENT FRAUD","Community report",Red);CommunityReportCard("KYC expiry message","IMPERSONATION","Community report",Violet);CommunityReportCard("Fake customer-care number","IMPERSONATION","Community report",Blue);Spacer(Modifier.height(12.dp));PrimaryButton("+  REPORT A SCAM",onReport);Spacer(Modifier.height(14.dp));Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF0D0A17)).border(1.dp,Color.White.copy(alpha=.06f),RoundedCornerShape(18.dp))){Row(Modifier.fillMaxWidth().clickable{showChakshu=!showChakshu}.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Info,null,tint=Cyan,modifier=Modifier.size(18.dp));Spacer(Modifier.width(10.dp));Text("WHAT IS CHAKSHU?",color=TextPrimary,fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text(if(showChakshu)"−" else "+",color=VioletBright,fontSize=20.sp)};if(showChakshu){Column(Modifier.padding(start=15.dp,end=15.dp,bottom=15.dp)){Text("Chakshu is the Government of India's facility on Sanchar Saathi for reporting suspected fraudulent communications. Reporting helps authorities identify and act on misuse of telecom resources.",color=TextMuted,fontSize=11.sp,lineHeight=17.sp);Spacer(Modifier.height(9.dp));Text("OPEN CHAKSHU →",color=Cyan,fontSize=10.sp,fontWeight=FontWeight.Bold,modifier=Modifier.clickable{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://sancharsaathi.gov.in/sfc/")))})}}}}}

@Composable
fun ReportScamScreen(onBack: () -> Unit) {
    var indicator by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var whenWhere by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader("REPORT A SCAM", onBack)

        Spacer(Modifier.height(18.dp))

        Text(
            "Help protect other users.",
            color = TextPrimary,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Tell us what you observed.",
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(20.dp))

        FormField(
            label = "SUSPICIOUS URL / NUMBER / INDICATOR",
            value = indicator,
            onValueChange = { indicator = it }
        )

        Spacer(Modifier.height(12.dp))

        FormField(
            label = "TYPE · MESSAGE / URL / QR / UPI",
            value = type,
            onValueChange = { type = it }
        )

        Spacer(Modifier.height(12.dp))

        FormField(
            label = "SCAM CATEGORY",
            value = category,
            onValueChange = { category = it }
        )

        Spacer(Modifier.height(12.dp))

        FormField(
            label = "WHEN / WHERE DID IT HAPPEN?",
            value = whenWhere,
            onValueChange = { whenWhere = it }
        )

        Spacer(Modifier.height(12.dp))

        FormField(
            label = "WHAT HAPPENED?",
            value = description,
            height = 150,
            onValueChange = { description = it }
        )

        Spacer(Modifier.height(18.dp))

        PrimaryButton(
            text = "SUBMIT REPORT",
            onClick = {}
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "-",
            color = TextDim,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable private fun CommunityReportCard(title:String,category:String,subtitle:String,accent:Color){Row(Modifier.fillMaxWidth().padding(bottom=9.dp).clip(RoundedCornerShape(18.dp)).background(Surface).padding(15.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(9.dp).clip(CircleShape).background(accent));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(title,color=TextPrimary,fontSize=13.sp,fontWeight=FontWeight.SemiBold);Text("$category  ·  $subtitle",color=TextDim,fontSize=10.sp)};Icon(Icons.Default.ArrowForward,null,tint=TextDim,modifier=Modifier.size(16.dp))}}
@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    height: Int = 62
) {
    Column {
        Text(
            label,
            color = TextDim,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(6.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Surface)
                .border(
                    1.dp,
                    Color.White.copy(alpha = .07f),
                    RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontSize = 13.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable private fun SectionLabel(text:String){Text(text,color=TextDim,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp)}
@Composable private fun ScreenHeader(title:String,onBack:()->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back",tint=TextMuted)};Text(title,color=TextPrimary,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=1.1.sp)}}
@Composable private fun PrimaryButton(text:String,onClick:()->Unit){Text(text,color=Color.White,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF7138D4),Color(0xFF5B6FFF)))).clickable(onClick=onClick).padding(vertical=15.dp))}
@Composable private fun SmallButton(text:String,accent:Color,onClick:()->Unit){Text(text,color=accent,fontSize=10.sp,fontWeight=FontWeight.Bold,modifier=Modifier.clip(RoundedCornerShape(12.dp)).background(Raised).clickable(onClick=onClick).padding(horizontal=15.dp,vertical=11.dp))}

package com.akslabs.circletosearch

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akslabs.circletosearch.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScamShieldHomeScreenV2(onScan: () -> Unit, onCommunity: () -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    val accessibilityEnabled = remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val bubbleEnabled = remember { mutableStateOf(prefs.getBoolean("bubble_enabled", false)) }

    Scaffold(containerColor = ShieldBackground, topBar = {
        TopAppBar(
            title = { Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(ShieldViolet.copy(.14f)).border(1.dp, ShieldViolet.copy(.35f), RoundedCornerShape(10.dp)), Alignment.Center) { Icon(Icons.Default.Shield, null, tint=ShieldVioletBright, modifier=Modifier.size(20.dp)) }
                Spacer(Modifier.width(9.dp)); Text("SCAMSHIELD", fontSize=18.sp, fontWeight=FontWeight.ExtraBold, letterSpacing=1.5.sp)
            }},
            actions={ IconButton(onClick=onSettings){ Icon(Icons.Default.Settings,"Settings",tint=ShieldMuted) } },
            colors=TopAppBarDefaults.topAppBarColors(containerColor=ShieldBackground, titleContentColor=ShieldText)
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=16.dp).verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(10.dp)) {
            // Hero: intentionally dense and central, rather than a conventional full-width dashboard card.
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(Brush.linearGradient(listOf(ShieldPurple.copy(.32f), ShieldSurfaceRaised, ShieldSurface))).border(1.dp, ShieldViolet.copy(.34f), RoundedCornerShape(30.dp)).padding(17.dp)) {
                Column(verticalArrangement=Arrangement.spacedBy(11.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Box(Modifier.size(76.dp).clip(CircleShape).background(ShieldBackground.copy(.48f)).border(1.5.dp, ShieldViolet.copy(.48f), CircleShape), Alignment.Center) {
                            Icon(Icons.Default.Shield, null, tint=ShieldVioletBright, modifier=Modifier.size(42.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("PROTECTION SYSTEM", fontSize=10.sp, fontWeight=FontWeight.Bold, color=ShieldDim, letterSpacing=1.7.sp)
                            Text(if(accessibilityEnabled.value) "ACTIVE" else "READY TO SET UP", fontSize=23.sp, fontWeight=FontWeight.ExtraBold, color=ShieldText)
                            Text(if(accessibilityEnabled.value) "Monitoring enabled" else "Device access required", fontSize=12.sp, color=ShieldMuted)
                        }
                    }
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(if(accessibilityEnabled.value) ShieldGreen else ShieldVioletBright))
                        Spacer(Modifier.width(7.dp)); Text(if(accessibilityEnabled.value) "ScamShield can inspect suspicious content across your phone." else "Enable access to inspect suspicious content across your phone.", fontSize=12.sp, color=ShieldMuted, lineHeight=17.sp, modifier=Modifier.weight(1f))
                    }
                    if(!accessibilityEnabled.value) Button(onClick={openAccessibilitySettings(context); accessibilityEnabled.value=true}, Modifier.fillMaxWidth().height(44.dp), shape=RoundedCornerShape(13.dp)) { Text("ENABLE PROTECTION", fontWeight=FontWeight.Bold, letterSpacing=.7.sp) }
                }
            }

            Text("SCAN", fontSize=10.sp, fontWeight=FontWeight.Bold, color=ShieldDim, letterSpacing=2.sp, modifier=Modifier.padding(start=3.dp, top=2.dp))
            Surface(onClick=onScan, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(25.dp), color=ShieldSurface, border=BorderStroke(1.dp, ShieldViolet.copy(.28f))) {
                Column(Modifier.padding(18.dp), verticalArrangement=Arrangement.spacedBy(11.dp)) {
                    Row(verticalAlignment=Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text("CHECK BEFORE YOU TRUST IT", fontSize=10.sp, fontWeight=FontWeight.Bold, color=ShieldVioletBright, letterSpacing=1.2.sp)
                            Text("Is it safe?", fontSize=30.sp, fontWeight=FontWeight.ExtraBold, color=ShieldText)
                            Text("Paste anything suspicious — URL, SMS, image or file.", fontSize=13.sp, color=ShieldMuted, lineHeight=18.sp)
                        }
                        Box(Modifier.size(43.dp).clip(CircleShape).background(ShieldViolet.copy(.12f)).border(1.dp, ShieldViolet.copy(.28f), CircleShape), Alignment.Center) { Icon(Icons.Default.Security,null,tint=ShieldVioletBright,modifier=Modifier.size(23.dp)) }
                    }
                    Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { TypeChip("URL"); TypeChip("SMS"); TypeChip("IMAGE"); TypeChip("FILE") }
                    Button(onClick=onScan, Modifier.fillMaxWidth().height(48.dp), shape=RoundedCornerShape(14.dp), colors=ButtonDefaults.buttonColors(containerColor=ShieldViolet, contentColor=ShieldBackground)) { Text("CHECK NOW", fontWeight=FontWeight.ExtraBold, letterSpacing=1.sp); Spacer(Modifier.width(7.dp)); Icon(Icons.Default.ArrowForward,null,Modifier.size(17.dp)) }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                ActionTile("COMMUNITY", "Intel & reports", Icons.Default.Groups, onCommunity, Modifier.weight(1f))
                ActionTile("FLOATING", if(bubbleEnabled.value) "Protection ON" else "Protection OFF", Icons.Default.Shield, { if(!accessibilityEnabled.value) openAccessibilitySettings(context) else { val next=!bubbleEnabled.value; bubbleEnabled.value=next; prefs.edit().putBoolean("bubble_enabled",next).apply() } }, Modifier.weight(1f))
            }
            Surface(shape=RoundedCornerShape(16.dp), color=ShieldSurfaceRaised, border=BorderStroke(1.dp, ShieldDim.copy(.16f)), modifier=Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal=13.dp, vertical=10.dp), verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield,null,tint=ShieldMuted,modifier=Modifier.size(19.dp)); Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text("QUICK ACCESS",fontSize=10.sp,fontWeight=FontWeight.Bold,color=ShieldDim,letterSpacing=1.sp); Text(if(bubbleEnabled.value) "Floating shield is on" else "Floating shield is off",fontSize=12.sp,color=ShieldMuted) }; Switch(checked=bubbleEnabled.value,onCheckedChange={v -> if(!accessibilityEnabled.value) openAccessibilitySettings(context) else { bubbleEnabled.value=v; prefs.edit().putBoolean("bubble_enabled",v).apply() }},enabled=accessibilityEnabled.value,colors=SwitchDefaults.colors(checkedThumbColor=ShieldBackground,checkedTrackColor=ShieldViolet))
                }
            }
            Spacer(Modifier.height(5.dp))
        }
    }
}

@Composable private fun TypeChip(text:String) { Surface(shape=RoundedCornerShape(9.dp), color=Color.Transparent, border=BorderStroke(1.dp,ShieldDim.copy(.30f))) { Text(text,Modifier.padding(horizontal=10.dp,vertical=6.dp),fontSize=9.sp,fontWeight=FontWeight.Bold,color=ShieldMuted,letterSpacing=.8.sp) } }

@Composable private fun ActionTile(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit,modifier:Modifier) {
    Surface(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(17.dp),color=ShieldSurfaceRaised,border=BorderStroke(1.dp,ShieldViolet.copy(.16f))) {
        Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) { Icon(icon,null,tint=ShieldVioletBright,modifier=Modifier.size(20.dp)); Text(title,fontSize=9.sp,fontWeight=FontWeight.Bold,color=ShieldDim,letterSpacing=1.1.sp); Text(subtitle,fontSize=11.sp,fontWeight=FontWeight.SemiBold,color=ShieldText) }
    }
}

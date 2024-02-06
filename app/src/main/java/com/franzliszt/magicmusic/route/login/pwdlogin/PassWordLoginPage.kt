package com.franzliszt.magicmusic.route.login.pwdlogin

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.pwdlogin.PwdLoginBean
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.flow.collectLatest


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PasswordLoginPage(
    viewModel: PwdLoginViewModel = hiltViewModel(),
    onLoginSuccess:()->Unit,
    onQrcode:()->Unit
){
    val userStatus = viewModel.userStatus.value
    val pwdStatus = viewModel.passwordStatus.value
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(key1 = scaffoldState.snackbarHostState){
        viewModel.eventFlow.collectLatest {
            when(it){
                is PwdLoginStatus.LoginEmpty-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is PwdLoginStatus.LoginFailed-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is PwdLoginStatus.LoginSuccess->  onLoginSuccess()
                is PwdLoginStatus.ForgetPassword-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is PwdLoginStatus.GoogleLogin-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is PwdLoginStatus.TwitterLogin-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is PwdLoginStatus.NetworkFailed-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(hostState = it) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        },
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MagicMusicTheme.colors.backgroundBottom,
                            MagicMusicTheme.colors.backgroundEnd,
                            MagicMusicTheme.colors.backgroundCenter
                        )
                    )
                )
        ) {
            IrregularBg()
            LoginMode(
                onQrcode = onQrcode,
                onGoogle = {viewModel.onEvent(PwdLoginEvent.GoogleLogin)},
                onTwitter = {viewModel.onEvent(PwdLoginEvent.TwitterLogin)}
            )
            PwdLoginView(
                userTextStatus = userStatus,
                pwdTextStatus = pwdStatus,
                onChangeUser = { viewModel.onEvent(PwdLoginEvent.ChangeUserName(it)) },
                onChangePwd = { viewModel.onEvent(PwdLoginEvent.ChangePassword(it)) },
                onChangePwdStatus = { viewModel.onEvent(PwdLoginEvent.ChangePwdStatus(it)) },
                onLogin = { viewModel.onEvent(PwdLoginEvent.PwdLogin) },
                onForgetPwd = { viewModel.onEvent(PwdLoginEvent.ForgetPassword) }
            )
        }
    }
}

@Composable 
private fun IrregularBg(
    topColor: Color = MagicMusicTheme.colors.backgroundTop,
    centerColor: Color = MagicMusicTheme.colors.backgroundCenter,
    bottomColor: Color = MagicMusicTheme.colors.backgroundBottom,
    iconToUp:ImageBitmap = ImageBitmap.imageResource(id = R.drawable.icon_login_arrow_up),
    iconToDown:ImageBitmap = ImageBitmap.imageResource(id = R.drawable.icon_login_arrow_down)
){
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.matchParentSize()){
            val bottomHeight:Dp = (size.height/6 * 5).toDp() //上半部分不规则图形高度，占整个屏幕高度5/6
            val leftHeight:Dp = (size.height/6).toDp() //上班部分的左右俩边不规则图形高度之差，占整个屏幕高度1/6
            //以坐标为基准，(0,0)为左上角
            //上半部分的不规则图形绘制
            val clipPathTop = Path().apply {
                lineTo(size.width,0f) //到右上角
                lineTo(size.width,bottomHeight.toPx()) //右下角，宽度：满，高度：减去下半部分的高度
                lineTo(0f,(bottomHeight-leftHeight).toPx()) //左下角，宽度：0，高度：减去下半部分的高度和右边的差
                close()
            }
            //下半部分的不规则图形绘制
            val clipPathBottom = Path().apply {
                lineTo(0f,bottomHeight.toPx()) //左上,宽度:0,高度：减去上下之差
                lineTo(size.width,size.height) //右下
                lineTo(0f,size.height) //左下
            }

            //上半部分不规则图形
            clipPath(path = clipPathTop){
                drawRect(
                    brush = Brush.verticalGradient(listOf(topColor,centerColor,bottomColor)), //垂直渐变
                    size = size
                )
            }

           //下半部分不规则图形
            clipPath(path = clipPathBottom){
                drawRect(
                    brush = Brush.horizontalGradient(listOf(topColor,bottomColor,bottomColor)), //水平渐变
                    size = size
                )
            }

            drawImage(
                image = iconToUp,
                //可选偏移量，表示绘制给定图片的目标位置的左上偏移量
                dstOffset = IntOffset(50,(size.height-120).toInt()),
                //目标图像的大小
                dstSize = IntSize(100,100)
            )

            drawImage(
                image = iconToDown,
                //可选偏移量，表示绘制给定图片的目标位置的左上偏移量
                dstOffset = IntOffset((size.width-120).toInt(),(size.height/6 * 5-150).toInt()),
                //目标图像的大小
                dstSize = IntSize(100,100)
            )
        }
    }
}

@Composable
private fun LoginMode(
    maxHeight:Dp = LocalConfiguration.current.screenHeightDp.dp,
    onGoogle: () -> Unit,
    onQrcode: () -> Unit,
    onTwitter: () -> Unit
){
    ConstraintLayout(
        modifier = Modifier
            .offset(0.dp, maxHeight / 6 * 4)
            .fillMaxWidth()
            .height(maxHeight / 6 * 2)
    ){
        val (icon_google,icon_qrcode,icon_twitter) = createRefs()
        Image(
            modifier = Modifier
                .size(maxHeight / 6 / 3 + 10.dp)
                .clickable { onGoogle() }
                .constrainAs(icon_google) {
                    top.linkTo(parent.top, maxHeight / 6/2)
                    start.linkTo(parent.start, 30.dp)
                },
            painter = painterResource(id = R.drawable.icon_login_google),
            contentDescription = "Google"
        )

        Image(
            modifier = Modifier
                .size(maxHeight / 6 / 3 + 15.dp)
                .clickable { onQrcode() }
                .constrainAs(icon_qrcode) {
                    start.linkTo(icon_google.start)
                    end.linkTo(icon_twitter.end)
                    top.linkTo(parent.top,10.dp)
                    bottom.linkTo(parent.bottom)
                },
            painter = painterResource(id = R.drawable.icon_login_qrcode),
            contentDescription = "QRCode",
        )

        Image(
            modifier = Modifier
                .size(maxHeight / 6 / 3 + 20.dp)
                .clickable { onTwitter() }
                .constrainAs(icon_twitter) {
                    bottom.linkTo(parent.bottom, maxHeight / 6/2-20.dp)
                    end.linkTo(parent.end, 30.dp)
                },
            painter = painterResource(id = R.drawable.icon_login_twitter),
            contentDescription = "Twitter",
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("ResourceType")
@Composable
private fun PwdLoginView(
    maxWidth:Int = LocalConfiguration.current.screenWidthDp,
    maxHeight:Int = LocalConfiguration.current.screenHeightDp,
    userTextStatus:PwdLoginUIStatus,
    pwdTextStatus:PwdLoginUIStatus,
    onChangeUser:(String)->Unit,
    onChangePwd:(String)->Unit,
    onChangePwdStatus:(Boolean)->Unit,
    onLogin:()->Unit,
    onForgetPwd:()->Unit
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier
        .fillMaxWidth()
        .height((maxHeight / 6 * 4).dp)
        .padding(top = 50.dp, start = 10.dp, end = 10.dp)
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }
    ) {
        Image(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally),
            painter = painterResource(id = R.drawable.magicmusic_logo),
            contentDescription = "MagicMusicLogo",
        )
        //账号输入框
        TextField(
            value = userTextStatus.text,
            onValueChange = onChangeUser,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.icon_login_user),
                    contentDescription = userTextStatus.label,
                    tint = MagicMusicTheme.colors.defaultIcon,
                    modifier = Modifier.size(24.dp)
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = MagicMusicTheme.colors.defaultIcon,//没有焦点时，输入框下方的分割线颜色
                focusedIndicatorColor = MagicMusicTheme.colors.defaultIcon,//拥有焦点时，输入框下方的分割线颜色
                errorIndicatorColor = Color.Transparent,
                cursorColor = MagicMusicTheme.colors.defaultIcon,//光标颜色
            ),
            textStyle = TextStyle(color = MagicMusicTheme.colors.textContent, fontSize = 12.sp),//输入框内的字体样式
            label = { Text(text = userTextStatus.label, color = MagicMusicTheme.colors.textTitle, style = MaterialTheme.typography.body1)},//上方提示字体样式
            placeholder = {Text(text = userTextStatus.hint, color = MagicMusicTheme.colors.textContent, style = MaterialTheme.typography.caption)},//hint的字体样式
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (!it.isFocused) {
                        keyboardController?.hide()
                    }
                }
        )

        Spacer(modifier = Modifier.height(20.dp))

        //密码输入框
        TextField(
            value = pwdTextStatus.text,
            onValueChange = onChangePwd,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.icon_login_pwd),
                    contentDescription = pwdTextStatus.label,
                    tint = MagicMusicTheme.colors.defaultIcon,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = { Icon(
                painter =  painterResource(id = if (pwdTextStatus.isShowPwd) R.drawable.icon_login_pwd_show else R.drawable.icon_login_pwd_hide),
                contentDescription = pwdTextStatus.label,
                tint =  MagicMusicTheme.colors.defaultIcon,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onChangePwdStatus(pwdTextStatus.isShowPwd) }
            ) },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = MagicMusicTheme.colors.defaultIcon,//没有焦点时，输入框下方的分割线颜色
                focusedIndicatorColor = MagicMusicTheme.colors.defaultIcon,//拥有焦点时，输入框下方的分割线颜色
                errorIndicatorColor = Color.Transparent,
                cursorColor = MagicMusicTheme.colors.defaultIcon,//光标颜色
            ),
            textStyle = TextStyle(color = MagicMusicTheme.colors.textContent, fontSize = 12.sp),//输入框内的字体样式
            label = { Text(text = pwdTextStatus.label, color = MagicMusicTheme.colors.textTitle, style = MaterialTheme.typography.body1)},//上方提示字体样式
            placeholder = {Text(text = pwdTextStatus.hint, color = MagicMusicTheme.colors.textContent, style = MaterialTheme.typography.caption)},//hint的字体样式
            keyboardOptions = KeyboardOptions(
                keyboardType = if (pwdTextStatus.isShowPwd) KeyboardType.Text else KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            }),
            visualTransformation = if (pwdTextStatus.isShowPwd) VisualTransformation.None else PasswordVisualTransformation(),//设置密文样式，默认为'.';可以修改PasswordVisualTransformation('*')
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (!it.isFocused) {
                        keyboardController?.hide()
                    }
                }
        )

        Spacer(modifier = Modifier.height(5.dp))

        //忘记密码
        Text(
            text = stringResource(id = R.string.login_forget_password),
            color = MagicMusicTheme.colors.textContent,
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onForgetPwd))

        Spacer(modifier = Modifier.height(40.dp))

        TextButton(
            modifier = Modifier
                .width((maxWidth - 100).dp)
                .height(40.dp)
                .align(Alignment.CenterHorizontally)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MagicMusicTheme.colors.loginStart,
                            MagicMusicTheme.colors.loginEnd
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            onClick = {
                onLogin()
                keyboardController?.hide()
            },
        ){
            Text(
                text = stringResource(id = R.string.login_button_text),
                color = MagicMusicTheme.colors.loginText,
                style = MaterialTheme.typography.button,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
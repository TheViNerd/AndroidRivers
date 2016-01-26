package com.silverkeytech.android_rivers.meta_weblog

import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.activities.Duration
import com.silverkeytech.android_rivers.activities.toastee
import org.holoeverywhere.app.Activity

fun showPostBlogDialog(context: Activity, onOK: (res: Array<DialogInput>) -> Unit) {
    val inputs = arrayOf(DialogInput(MULTI_LINE_INPUT, "Post", "", null))

    val dlg = createFlexibleInputDialog(context, "Write", inputs) {
        d, res ->
        onOK(res)
        d?.dismiss()
    }
    dlg.show()
}

fun showPostBlogDialogWithContent(context: Activity, content: String, onOK: (res: Array<DialogInput>) -> Unit) {
    val inputs = arrayOf(DialogInput(MULTI_LINE_INPUT, "Post", content, null))

    val dlg = createFlexibleInputDialog(context, "Write", inputs) {
        d, res ->
        onOK(res)
        d?.dismiss()
    }
    dlg.show()
}


fun showBlogConfigurationDialog(context: Activity, onOK: (res: Array<DialogInput>) -> Unit) {
    val inputs = arrayOf(DialogInput(NORMAL_INPUT, "Server", "androidrivers.wordpress.com", textValidator() {
        str ->
        if (str.isNullOrBlank()){
            context.toastee("Server is required", Duration.LONG)
        }
    }),
            DialogInput(NORMAL_INPUT, "Username", PRIVATE_BLOG_USERNAME, textValidator() {
                str ->
                if (str.isNullOrBlank()){
                    context.toastee("Username is required", Duration.LONG)
                }
            }),
            DialogInput(PASSWORD_INPUT, "Password", PRIVATE_BLOG_PASSWORD, textValidator() {
                str ->
                if (str.isNullOrBlank()){
                    context.toastee("Password is required", Duration.LONG)
                }
            })
    )

    val dlg = createFlexibleInputDialog(context, "Connect to your blog", inputs) {
        d, res ->
        onOK(res)
        d?.dismiss()
    }
    dlg.show()
}


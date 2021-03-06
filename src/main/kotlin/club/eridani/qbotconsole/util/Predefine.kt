package club.eridani.qbotconsole.util

import club.eridani.qbotconsole.VERSION

val gradleKts = """plugins {
    kotlin("jvm") version "1.4.30"
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/eridani/maven")
}
//想要copy 一个库 到 libs
//只需要将平时的 implementation("blablabla:blablabl:blablab") 的 implementation 改成 copyLib
val copyLib by configurations.creating

configurations {
    implementation.get().extendsFrom(copyLib)
}

dependencies {

    fileTree("libs") {
        forEach {
            implementation(files(it))
        }
    }

    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("script-runtime"))
    val miraiVersion = "2.3.2" // 替换为你需要的版本号
    api("net.mamoe", "mirai-core-api", miraiVersion)     // 编译代码使用
    runtimeOnly("net.mamoe", "mirai-core", miraiVersion) // 运行时使用
    compileOnly("club.eridani:qbotconsole:$VERSION")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("${'$'}{projectDir.absolutePath}/scripts"))
        }
    }
}

tasks.create<Copy>("copyLibs") {
    from(copyLib)
    into("libs")
}

"""


val miraiJs = """!(() => {
    let prompt = window.prompt;

    // jsbridge://CAPTCHA/onVerifyCAPTCHA?p=....#2
    /**
     * @type {string} url
     * @return {boolean}
     */
    function processUrl(url) {
        let prefix = "jsbridge://CAPTCHA/onVerifyCAPTCHA?p="
        if (url.startsWith(prefix)) {
            let json = url.substring(prefix.length);
            for (let i = json.length; i--; i > 0) {
                let j = json.substr(0, i)
                console.log(j);
                try {
                    let content = decodeURIComponent(j);
                    let obj = JSON.parse(content);
                    console.log(obj);
                    window.miraiSeleniumComplete = content;
                    prompt("MiraiSelenium - ticket", obj.ticket)
                    break;
                } catch (ignore) {
                }
            }
            return true;
        }
        return false;
    }

    (() => {
        let desc = Object.getOwnPropertyDescriptor(Image.prototype, "src");
        Object.defineProperty(Image.prototype, "src", {
            get: desc.get,
            set(v) {
                if (processUrl(v)) return;
                desc.set.call(this, v)
            }
        })
    })();


    (() => {
        let desc = Object.getOwnPropertyDescriptor(HTMLIFrameElement.prototype, "src");
        Object.defineProperty(HTMLIFrameElement.prototype, "src", {
            get: desc.get,
            set(v) {
                if (processUrl(v)) return;
                desc.set.call(this, v)
            }
        })
    })();

    (() => {
        let UserAgent = "$\{MIRAI_SELENIUM-USERAGENT}";
        if (UserAgent !== "$\{MIRAI_SELENIUM-USERAGENT}") {
            Object.defineProperty(Navigator.prototype, "userAgent", {
                get() {
                    return UserAgent
                }
            });
            document.querySelectorAll("script").forEach(it => it.remove());
        }
    })();
})()"""
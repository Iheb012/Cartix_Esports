package services;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

public class CallbackServer {

    private static volatile String code;
    private static HttpServer server;
    private static final int PORT = 8091;

    // Latch released when /done is hit — keeps server alive until browser loads it
    private static CountDownLatch doneLatch;

    public static synchronized void reset() {
        code = null;
        if (server != null) {
            server.stop(0);
            server = null;
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        }
        // Force kill any process still using the port
        try {
            Runtime.getRuntime().exec(
                    "cmd /c for /f \"tokens=5\" %a in " +
                            "('netstat -ano ^| findstr :8091') do taskkill /PID %a /F");
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }

    public static synchronized void start() throws IOException {
        if (server != null) return;

        doneLatch = new CountDownLatch(1);
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Step 1 — Discord redirects here with the auth code
        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("code=")) {
                code = query.split("code=")[1].split("&")[0];
            }

            String html = getSuccessHtml();
            byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlBytes.length);
            exchange.getResponseBody().write(htmlBytes);
            exchange.getResponseBody().close();
        });

        // Step 2 — success page navigates here after 3 s or on button click.
        // Server stays alive until this is hit, so no "page unreachable".
        server.createContext("/done", exchange -> {
            String html = getDoneHtml();
            byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlBytes.length);
            exchange.getResponseBody().write(htmlBytes);
            exchange.getResponseBody().close();
            doneLatch.countDown();
        });

        server.start();

        // Shut down server only after browser has loaded /done
        new Thread(() -> {
            try { doneLatch.await(); } catch (InterruptedException ignored) {}
            server.stop(0);
            server = null;
        }).start();
    }

    public static String getCode() {
        return code;
    }

    /** Loads /logoteam.png from classpath and returns a base64 data URI. */
    private static String getLogoDataUri() {
        try (InputStream img = CallbackServer.class.getResourceAsStream("/logoteam.png")) {
            if (img == null) return "";
            return "data:image/png;base64,"
                    + Base64.getEncoder().encodeToString(img.readAllBytes());
        } catch (Exception e) {
            return "";
        }
    }

    private static String getSuccessHtml() {
        String logoDataUri = getLogoDataUri();
        String logoHtml = logoDataUri.isEmpty()
                ? "<div class='logo'>CAR<span>TIX</span></div>"
                : "<div class='logo'>"
                + "<img src='" + logoDataUri + "' class='logo-img' alt='Cartix'/>"
                + "CAR<span>TIX</span>"
                + "</div>";

        return "<!DOCTYPE html><html><head>"
                + "<title>Cartix - Login Successful</title>"
                + "<meta charset='UTF-8'>"
                + "<style>"
                + "* { margin:0; padding:0; box-sizing:border-box; }"
                + "body { font-family:'Segoe UI',sans-serif; background:#0d1117;"
                + "  display:flex; justify-content:center; align-items:center;"
                + "  height:100vh; overflow:hidden; }"
                + ".orb1 { position:fixed; width:300px; height:300px;"
                + "  background:radial-gradient(circle,rgba(77,120,255,0.3),transparent);"
                + "  border-radius:50%; top:-50px; left:-50px; pointer-events:none; }"
                + ".orb2 { position:fixed; width:480px; height:480px;"
                + "  background:radial-gradient(circle,rgba(120,50,200,0.2),transparent);"
                + "  border-radius:50%; bottom:-100px; left:30%; pointer-events:none; }"
                + ".card { background:rgba(22,26,46,0.95);"
                + "  border:1px solid rgba(77,120,255,0.2); border-radius:20px;"
                + "  padding:48px 40px; text-align:center; max-width:420px; width:90%;"
                + "  position:relative; z-index:10; box-shadow:0 20px 60px rgba(0,0,0,0.5); }"
                + ".logo { display:flex; align-items:center; justify-content:center;"
                + "  gap:10px; font-size:28px; font-weight:800; color:white;"
                + "  letter-spacing:2px; margin-bottom:32px; }"
                + ".logo span { color:#4d78ff; }"
                + ".logo-img { height:36px; width:auto; object-fit:contain;"
                + "  filter:drop-shadow(0 0 6px rgba(77,120,255,0.4)); }"
                + ".check { width:72px; height:72px; background:rgba(34,217,138,0.15);"
                + "  border:2px solid #22d98a; border-radius:50%; display:flex;"
                + "  align-items:center; justify-content:center; margin:0 auto 24px;"
                + "  font-size:32px; color:#22d98a; }"
                + "h1 { color:white; font-size:22px; font-weight:700; margin-bottom:12px; }"
                + "p { color:#9aa3c7; font-size:14px; line-height:1.6; margin-bottom:8px; }"
                + ".timer { color:#4d78ff; font-size:12px; margin-bottom:24px; }"
                + ".btn { background:linear-gradient(135deg,#4d78ff,#7c4dff); color:white;"
                + "  border:none; padding:12px 32px; border-radius:10px; font-size:14px;"
                + "  font-weight:600; cursor:pointer; letter-spacing:0.5px; transition:opacity 0.2s; }"
                + ".btn:hover { opacity:0.85; }"
                + "</style>"
                + "<script>"
                + "function dismiss() {"
                + "  window.location.href='http://localhost:" + PORT + "/done';"
                + "}"
                + "var seconds=3;"
                + "function countdown() {"
                + "  var el=document.getElementById('timer');"
                + "  if(seconds<=0){ dismiss(); return; }"
                + "  if(el) el.innerText='Closing in '+seconds+' second'+(seconds!==1?'s':'')+'...';"
                + "  seconds--;"
                + "  setTimeout(countdown,1000);"
                + "}"
                + "window.onload=countdown;"
                + "</script>"
                + "</head><body>"
                + "<div class='orb1'></div><div class='orb2'></div>"
                + "<div class='card'>"
                + logoHtml
                + "<div class='check'>&#10003;</div>"
                + "<h1>Authentication Successful!</h1>"
                + "<p>You have successfully signed in with Discord.</p>"
                + "<p>You can now return to the app.</p>"
                + "<p class='timer' id='timer'>Closing in 3 seconds...</p>"
                + "<button class='btn' onclick='dismiss()'>Close Window</button>"
                + "</div></body></html>";
    }

    private static String getDoneHtml() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Cartix</title>"
                + "<style>* { margin:0; padding:0; box-sizing:border-box; }"
                + "body { background:#0d1117; display:flex; align-items:center;"
                + "  justify-content:center; height:100vh;"
                + "  font-family:'Segoe UI',sans-serif; color:#9aa3c7; font-size:14px; }"
                + "</style></head><body><p>You may close this tab.</p></body></html>";
    }
}
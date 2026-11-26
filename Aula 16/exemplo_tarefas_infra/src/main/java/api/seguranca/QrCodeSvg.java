package api.seguranca;

import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

// Aula 15: desenha um QR Code como imagem SVG (texto), para a tela mostrar.
//
// O ZXing calcula a matriz de quadradinhos (BitMatrix); aqui só transformamos
// cada quadradinho preto num retângulo do SVG. O QR Code é gerado NO SERVIDOR:
// o segredo não vai para nenhum site de "gerar QR Code" na internet.
public final class QrCodeSvg {

    private QrCodeSvg() {
    }

    public static String gerar(String conteudo) {
        try {
            BitMatrix matriz = new QRCodeWriter().encode(conteudo, BarcodeFormat.QR_CODE, 0, 0,
                    // M = até 15% do código pode estar borrado e ainda ser lido. Margem de 4 módulos (padrão).
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, EncodeHintType.MARGIN, 4));
            int largura = matriz.getWidth();
            StringBuilder caminho = new StringBuilder();
            for (int y = 0; y < matriz.getHeight(); y++) {
                for (int x = 0; x < largura; x++) {
                    if (matriz.get(x, y)) {
                        // "M x y h1 v1 h-1 z": um quadrado de 1x1 na posição (x, y).
                        caminho.append("M").append(x).append(' ').append(y).append("h1v1h-1z");
                    }
                }
            }
            return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 " + largura + " " + matriz.getHeight()
                    + "\" shape-rendering=\"crispEdges\"><rect width=\"100%\" height=\"100%\" fill=\"#fff\"/>"
                    + "<path fill=\"#000\" d=\"" + caminho + "\"/></svg>";
        } catch (WriterException e) {
            throw new IllegalStateException("Não foi possível gerar o QR Code", e);
        }
    }
}

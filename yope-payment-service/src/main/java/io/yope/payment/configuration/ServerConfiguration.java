/**
 *
 */
package io.yope.payment.configuration;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author massi
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class ServerConfiguration {

    String url;

    String imagePath;

    @Getter
    @Setter
    String imageFolder;

    @Getter
    BigDecimal blockChainFees;

    public void setUrl(final String url) {
        this.url = url.endsWith("/")? url.substring(0, url.length()-1):url;
    }

    public void setImagePath(final String imagePath) {
        this.imagePath = imagePath.startsWith("/")? imagePath : "/" + imagePath;
        this.imagePath = this.imagePath.endsWith("/")? this.imagePath : this.imagePath + "/";
    }

    public String getUrl() {
        return url;
    }

    public String getImageAbsolutePath() {
        return getUrl() + getImagePath();
    }

    public String getImagePath() {
        return imagePath.startsWith("/")? imagePath : "/" + imagePath;
    }

}

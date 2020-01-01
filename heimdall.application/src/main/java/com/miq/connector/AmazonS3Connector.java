package com.miq.connector;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * The Class AmazonS3Connector.
 */
public class AmazonS3Connector {

  /**
   * The Constant LOGGER.
   */
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AmazonS3Connector.class);


  /**
   * The Constant CANT_BE_NULL_OR_EMPTY.
   */
  private static final String CANT_BE_NULL_OR_EMPTY = "can't be null or empty";

  /**
   * The Constant THE_ARGUMENT.
   */
  private static final String THE_ARGUMENT = "The argument ";
  /**
   * The region field, optional
   */
  private final Regions region;
  /**
   * The credentials field, optional if IAM role is configured.
   */
  private final AWSCredentials credentials;
  /**
   * The S3 client.
   */
  private AmazonS3 s3Client;

  /**
   * Instantiates a new amazon S3 connector.
   *
   * @param builder, the AmazonS3Connector builder
   */
  private AmazonS3Connector(final Builder builder) {
    this.region = builder.region;
    this.credentials = builder.credentials;
  }

  /**
   * Gets the S3 client.
   *
   * @return the S3 client
   */
  public AmazonS3 getS3Client() {
    return s3Client;
  }

  /**
   * Gets the region.
   *
   * @return the region
   */
  public Regions getRegion() {
    return region;
  }

  /**
   * Gets the credentials.
   *
   * @return the credentials
   */
  public AWSCredentials getCredentials() {
    return credentials;
  }

  /**
   * Upload single file.
   *
   * @param pathToSrcFile the file path
   * @param bucketName    the bucket name
   * @param key           the key name/folder
   * @throws InterruptedException
   */
  public void uploadFile(final String pathToSrcFile, final String bucketName, final String key)
      throws InterruptedException {
    if (StringUtils.isEmpty(pathToSrcFile)) {
      throw new IllegalArgumentException(THE_ARGUMENT + pathToSrcFile + CANT_BE_NULL_OR_EMPTY);
    }
    if (StringUtils.isEmpty(bucketName)) {
      throw new IllegalArgumentException(THE_ARGUMENT + bucketName + CANT_BE_NULL_OR_EMPTY);
    }
    if (StringUtils.isEmpty(key)) {
      throw new IllegalArgumentException(THE_ARGUMENT + key + CANT_BE_NULL_OR_EMPTY);
    }
    final File file = new File(pathToSrcFile);
    final TransferManager xferMgr =
        TransferManagerBuilder.standard().withS3Client(s3Client).build();
    final Upload xfer = xferMgr.upload(bucketName, key, file);
    this.showTransferProgress(xfer);
    this.waitForCompletion(xfer);
  }

  /**
   * Wait for completion.
   *
   * @param xfer the xfer
   * @throws InterruptedException
   */
  private void waitForCompletion(final Transfer xfer) throws InterruptedException {
    xfer.waitForCompletion();
  }

  /**
   * Show transfer progress.
   *
   * @param xfer the xfer
   */
  // Prints progress while waiting for the transfer to finish.
  private void showTransferProgress(final Transfer xfer) {
    // snippet-start:[s3.java1.s3_xfer_mgr_progress.poll]
    // print the transfer's human-readable description
    LOGGER.debug(xfer.getDescription());
    // print an empty progress bar...
    printProgressBar(0.0);
    // update the progress bar while the xfer is ongoing.
    do {
      try {
        Thread.sleep(100);
      } catch (final InterruptedException e) {
        return;
      }

      final TransferProgress progress = xfer.getProgress();
      final double pct = progress.getPercentTransferred();
      eraseProgressBar();
      printProgressBar(pct);
    } while (!xfer.isDone());
    // print the final state of the transfer.
    final TransferState xferState = xfer.getState();
    LOGGER.debug(": " + xferState);
    // snippet-end:[s3.java1.s3_xfer_mgr_progress.poll]
  }

  /**
   * Prints the progress bar.
   *
   * @param pct the pct
   */
  // prints a simple text progressbar: [##### ]
  private void printProgressBar(final double pct) {
    // if bar_size changes, then change erase_bar (in eraseProgressBar) to
    // match.
    final int bar_size = 40;
    final String empty_bar = "                                        ";
    final String filled_bar = "########################################";
    final int amtFull = (int) (bar_size * (pct / 100.0));
    LOGGER.debug(String.format("  [%s%s]%n", filled_bar.substring(0, amtFull),
        empty_bar.substring(0, bar_size - amtFull)));
  }

  /**
   * Erase progress bar.
   */
  private void eraseProgressBar() {
    // erase_bar is bar_size (from printProgressBar) + 4 chars.amap
    final String erase_bar =
        "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";
    LOGGER.debug(String.format(erase_bar));
  }


  /**
   * The Class Builder.
   */
  public static class Builder {

    /**
     * The region.
     */
    // default region is US_EAST_1
    private Regions region;

    /**
     * The credentials.
     */
    private AWSCredentials credentials;

    /**
     * Instantiates a new builder.
     */
    public Builder() {
      this.region = Regions.US_EAST_1;
    }

    /**
     * Sets the credential.
     *
     * @param accessKey the access key
     * @param secretKey the secret key
     * @return the builder
     */
    public Builder setCredentials(final String accessKey, final String secretKey) {
      this.credentials = new BasicAWSCredentials(accessKey, secretKey);
      return this;
    }

    /**
     * Sets the region.
     *
     * @param region the region
     * @return the builder
     */
    public Builder setRegion(final Regions region) {
      this.region = region;
      return this;
    }

    /**
     * Builds the S3 Connector.
     *
     * @return the amazon S3 connector
     */
    public AmazonS3Connector build() {
      final AmazonS3Connector s3Connector = new AmazonS3Connector(this);
      if (s3Connector.getCredentials() == null) {
        this.createS3ClientUsingIAMRole(s3Connector);
      } else {
        this.createS3ClientUsingCredentials(s3Connector);
      }
      return s3Connector;
    }

    /**
     * Creates the S3 client using IAM role.
     *
     * @param s3Connector the S3 Connector
     */
    private void createS3ClientUsingIAMRole(final AmazonS3Connector s3Connector) {
      s3Connector.s3Client = AmazonS3ClientBuilder.standard().withRegion(region)
          .withCredentials(DefaultAWSCredentialsProviderChain.getInstance()).build();
      LOGGER.info("Created S3Client object with IAM role");
    }

    /**
     * Creates the S3 client using credentials.
     *
     * @param s3Connector the S3 connector
     */
    private void createS3ClientUsingCredentials(final AmazonS3Connector s3Connector) {
      LOGGER.info("Local environment, so using access/secret key to create S3 client");
      s3Connector.s3Client = AmazonS3ClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .withRegion(s3Connector.region).build();
    }
  }
}

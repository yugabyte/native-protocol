/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.protocol.internal.request;

import static com.datastax.oss.protocol.internal.Assertions.assertThat;

import com.datastax.oss.protocol.internal.Message;
import com.datastax.oss.protocol.internal.MessageTestBase;
import com.datastax.oss.protocol.internal.PrimitiveSizes;
import com.datastax.oss.protocol.internal.TestDataProviders;
import com.datastax.oss.protocol.internal.binary.MockBinaryString;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class PrepareTest extends MessageTestBase<Prepare> {

  public PrepareTest() {
    super(Prepare.class);
  }

  @Override
  protected Message.Codec newCodec(int protocolVersion) {
    return new Prepare.Codec(protocolVersion);
  }

  @Test
  @UseDataProvider(location = TestDataProviders.class, value = "protocolV3OrV4")
  public void should_encode_and_decode_in_protocol_v3_or_v4(int protocolVersion) {
    Prepare initial = new Prepare("SELECT * FROM foo");

    MockBinaryString encoded = encode(initial, protocolVersion);

    assertThat(encoded).isEqualTo(new MockBinaryString().longString("SELECT * FROM foo"));
    assertThat(encodedSize(initial, protocolVersion))
        .isEqualTo(PrimitiveSizes.INT + "SELECT * FROM foo".length());

    Prepare decoded = decode(encoded, protocolVersion);

    assertThat(decoded.cqlQuery).isEqualTo(initial.cqlQuery);
  }

  @Test
  @UseDataProvider(location = TestDataProviders.class, value = "protocolV5OrAbove")
  public void should_encode_and_decode_with_keyspace_in_protocol_v5_or_above(int protocolVersion) {
    Prepare initial = new Prepare("SELECT * FROM foo", "ks");

    MockBinaryString encoded = encode(initial, protocolVersion);

    assertThat(encoded)
        .isEqualTo(new MockBinaryString().longString("SELECT * FROM foo").int_(0x01).string("ks"));
    assertThat(encodedSize(initial, protocolVersion))
        .isEqualTo(
            (PrimitiveSizes.INT + "SELECT * FROM foo".length())
                + PrimitiveSizes.INT
                + (PrimitiveSizes.SHORT + "ks".length()));

    Prepare decoded = decode(encoded, protocolVersion);

    assertThat(decoded.cqlQuery).isEqualTo(initial.cqlQuery);
    assertThat(decoded.keyspace).isEqualTo(initial.keyspace);
  }

  @Test
  @UseDataProvider(location = TestDataProviders.class, value = "protocolV5OrAbove")
  public void should_encode_and_decode_without_keyspace_in_protocol_v5_or_above(
      int protocolVersion) {
    Prepare initial = new Prepare("SELECT * FROM foo");

    MockBinaryString encoded = encode(initial, protocolVersion);

    assertThat(encoded)
        .isEqualTo(new MockBinaryString().longString("SELECT * FROM foo").int_(0x00));
    assertThat(encodedSize(initial, protocolVersion))
        .isEqualTo((PrimitiveSizes.INT + "SELECT * FROM foo".length()) + PrimitiveSizes.INT);

    Prepare decoded = decode(encoded, protocolVersion);

    assertThat(decoded.cqlQuery).isEqualTo(initial.cqlQuery);
    assertThat(decoded.keyspace).isEqualTo(initial.keyspace);
  }
}

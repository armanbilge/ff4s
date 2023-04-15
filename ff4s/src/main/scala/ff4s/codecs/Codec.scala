/*
 * Copyright 2022 buntec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2017 Nikita Gazarov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 */

package ff4s.codecs

// Verbatim from https://github.com/raquo/Laminar

/** This trait represents a way to encode and decode HTML attribute or DOM
  * property values.
  *
  * It is needed because attributes encode all values as strings regardless of
  * their type, and then there are also multiple ways to encode e.g. boolean
  * values. Some attributes encode those as "true" / "false" strings, others as
  * presence or absence of the element, and yet others use "yes" / "no" or "on"
  * / "off" strings, and properties encode booleans as actual booleans.
  *
  * Scala DOM Types hides all this mess from you using codecs. All those
  * pseudo-boolean attributes would be simply `Attr[Boolean](name, codec)` in
  * your code.
  */
trait Codec[ScalaType, DomType] {

  /** Convert the result of a `dom.Node.getAttribute` call to appropriate Scala
    * type.
    *
    * Note: HTML Attributes are generally optional, and `dom.Node.getAttribute`
    * will return `null` if an attribute is not defined on a given DOM node.
    * However, this decoder is only intended for cases when the attribute is
    * defined.
    */
  def decode(domValue: DomType): ScalaType

  /** Convert desired attribute value to appropriate DOM type. The resulting
    * value should be passed to `dom.Node.setAttribute` call, EXCEPT when
    * resulting value is a `null`. In that case you should call
    * `dom.Node.removeAttribute` instead.
    *
    * We use `null` instead of [[Option]] here to reduce overhead in JS land.
    * This method should not be called by end users anyway, it's the consuming
    * library's job to call this method under the hood.
    */
  def encode(scalaValue: ScalaType): DomType
}

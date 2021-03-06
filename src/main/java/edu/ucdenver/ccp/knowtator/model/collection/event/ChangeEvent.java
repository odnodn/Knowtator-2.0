/*
 * MIT License
 *
 * Copyright (c) 2018 Harrison Pielke-Lombardo
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.ucdenver.ccp.knowtator.model.collection.event;

import edu.ucdenver.ccp.knowtator.model.BaseModel;
import java.util.Optional;

/**
 * The type Change event.
 *
 * @param <O> the type parameter
 */
public class ChangeEvent<O> {

  private final O oldObject;
  private final O newObject;
  private final BaseModel model;

  /**
   * Instantiates a new Change event.
   *
   * @param model the model
   * @param oldObject the old object
   * @param newObject the new object
   */
  public ChangeEvent(BaseModel model, O oldObject, O newObject) {
    this.model = model;

    this.oldObject = oldObject;
    this.newObject = newObject;
  }

  /**
   * Gets old.
   *
   * @return the old
   */
  public Optional<O> getOld() {
    return Optional.ofNullable(oldObject);
  }

  /**
   * Gets new.
   *
   * @return the new
   */
  public Optional<O> getNew() {
    return Optional.ofNullable(newObject);
  }

  /**
   * Gets model.
   *
   * @return the model
   */
  public BaseModel getModel() {
    return model;
  }
}

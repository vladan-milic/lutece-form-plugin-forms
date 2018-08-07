/*
 * Copyright (c) 2002-2018, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */

package fr.paris.lutece.plugins.forms.web.admin;

import fr.paris.lutece.plugins.forms.business.Control;
import fr.paris.lutece.plugins.forms.business.ControlHome;
import fr.paris.lutece.plugins.forms.business.ControlType;
import fr.paris.lutece.plugins.forms.business.FormDisplay;
import fr.paris.lutece.plugins.forms.business.FormDisplayHome;
import fr.paris.lutece.plugins.forms.business.Group;
import fr.paris.lutece.plugins.forms.business.GroupHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.forms.business.Step;
import fr.paris.lutece.plugins.forms.business.StepHome;
import fr.paris.lutece.plugins.forms.business.Transition;
import fr.paris.lutece.plugins.forms.business.TransitionHome;
import fr.paris.lutece.plugins.forms.service.EntryServiceManager;
import fr.paris.lutece.plugins.forms.util.FormsConstants;
import fr.paris.lutece.plugins.forms.validation.IValidator;
import fr.paris.lutece.plugins.genericattributes.business.EntryType;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.url.UrlItem;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * This class provides the user interface to manage Form controls ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageControls.jsp", controllerPath = "jsp/admin/plugins/forms/", right = "FORMS_MANAGEMENT" )
public class FormControlJspBean extends AbstractJspBean
{

    private static final long serialVersionUID = -9023450166890042022L;

    private static final String TEMPLATE_MODIFY_TRANSITION_CONTROL = "/admin/plugins/forms/modify_transition_control.html";
    private static final String TEMPLATE_MODIFY_CONDITION_CONTROL = "/admin/plugins/forms/modify_condition_control.html";
    private static final String TEMPLATE_MODIFY_QUESTION_CONTROL = "/admin/plugins/forms/modify_question_control.html";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MODIFY_CONTROL = "forms.modify_control.pageTitle";

    // Validations
    private static final String CONTROL_VALIDATION_ATTRIBUTES_PREFIX = "forms.model.entity.control.attribute.";

    // Views
    private static final String VIEW_MODIFY_CONTROL = "modifyControl";
    private static final String VIEW_MODIFY_QUESTION_CONTROL = "modifyQuestionControl";
    private static final String VIEW_MODIFY_TRANSITION_CONTROL = "modifyTransitionControl";
    private static final String VIEW_MODIFY_CONDITION_CONTROL = "modifyConditionControl";
    private static final String VIEW_CONFIRM_REMOVE_CONTROL = "confirmRemoveControl";

    // Actions
    private static final String ACTION_MODIFY_CONTROL = "modifyControl";
    private static final String ACTION_REMOVE_CONTROL = "removeControl";
    private static final String ACTION_CANCEL_AND_RETURN = "cancelAndReturn";

    // Infos
    private static final String INFO_CONTROL_CREATED = "forms.info.control.created";
    private static final String INFO_CONTROL_UPDATED = "forms.info.control.updated";
    private static final String INFO_CONTROL_REMOVED = "forms.info.control.removed";
    private static final String MESSAGE_CONFIRM_REMOVE_CONTROL = "forms.message.confirmRemoveControl";
    private static final String INFO_CONDITION_GROUP_TITLE = "forms.modify_condition_group_control.title";
    private static final String INFO_CONDITION_QUESTION_TITLE = "forms.modify_condition_question_control.title";

    // Errors
    private static final String ERROR_CONTROL_REMOVED = "forms.error.deleteControl";
    private static final String ERROR_QUESTION_VALIDATOR_MATCH = "forms.error.control.validatorMatch";
    private static final String ERROR_VALIDATOR_VALUE_MATCH = "forms.error.control.valueMatch";
    
    // Session variable to store working values
    private Transition _transition;
    private Question _question;
    private Group _group;
    private Control _control;
    private Step _step;
    private String _strControlTemplate;
    private String _strControlTitle;

    
    /**
     * Returns the form to modify a control for question validation
     *
     * @param request
     *            The Http request
     * @return the html code of the control form
     */
    @View( VIEW_MODIFY_QUESTION_CONTROL )
    public String getModifyQuestionControl( HttpServletRequest request )
    {
    	int nIdStep = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_STEP ), FormsConstants.DEFAULT_ID_VALUE );
    	
    	_step = StepHome.findByPrimaryKey( nIdStep );

        if ( _step == null )
        {
            return redirectToViewManageForm( request );
        }
    	
    	int nIdQuestion = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_QUESTION ), FormsConstants.DEFAULT_ID_VALUE );

        _question = QuestionHome.findByPrimaryKey( nIdQuestion );

        if ( _question == null )
        {
            return redirectToViewManageForm( request );
        }

        _control = ControlHome.getControlByQuestionAndType( _question.getId( ), ControlType.VALIDATION.getLabel( ) );

        if ( _control == null )
        {
            _control = new Control( );
            _control.setIdQuestion( nIdQuestion );
            _control.setControlType( ControlType.VALIDATION.getLabel( ) );
        }
        
        _strControlTemplate = TEMPLATE_MODIFY_QUESTION_CONTROL;
        
        return redirectView( request, VIEW_MODIFY_CONTROL );
    }
    
    /**
     * Returns the form to modify a control for question validation
     *
     * @param request
     *            The Http request
     * @return the html code of the control form
     */
    @View( VIEW_MODIFY_TRANSITION_CONTROL )
    public String getModifyTransitionControl( HttpServletRequest request )
    {
    	int nIdStep = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_STEP ), FormsConstants.DEFAULT_ID_VALUE );
    	
    	_step = StepHome.findByPrimaryKey( nIdStep );

        if ( _step == null )
        {
            return redirectToViewManageForm( request );
        }
        
    	int nIdTransition = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_TRANSITION ), FormsConstants.DEFAULT_ID_VALUE );

    	_transition = TransitionHome.findByPrimaryKey( nIdTransition );

        if ( _transition == null )
        {
            return redirectToViewManageForm( request );
        }

        _control = ControlHome.findByPrimaryKey( _transition.getIdControl( ) );

        if ( _control == null )
        {
            _control = new Control( );
            _control.setControlType( ControlType.TRANSITION.getLabel( ) );
        }
        
        _strControlTemplate = TEMPLATE_MODIFY_TRANSITION_CONTROL;
        
        return redirectView( request, VIEW_MODIFY_CONTROL );
    }
    
    /**
     * Returns the form to modify a control for conditionnal question
     *
     * @param request
     *            The Http request
     * @return the html code of the control form
     */
    @View( VIEW_MODIFY_CONDITION_CONTROL )
    public String getModifyConditionControl( HttpServletRequest request )
    {
    	int nIdStep = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_STEP ), FormsConstants.DEFAULT_ID_VALUE );
    	
    	_step = StepHome.findByPrimaryKey( nIdStep );

        if ( _step == null )
        {
            return redirectToViewManageForm( request );
        }
        
    	int nIdCompositeDisplay = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_DISPLAY ), FormsConstants.DEFAULT_ID_VALUE );
    	
    	FormDisplay formDisplay = FormDisplayHome.findByPrimaryKey( nIdCompositeDisplay );
    	
    	if ( formDisplay == null )
        {
            return redirectToViewManageForm( request );
        }
    	
    	if( formDisplay.getCompositeType( ).equals( FormsConstants.COMPOSITE_QUESTION_TYPE ) )
    	{
    		_question = QuestionHome.findByPrimaryKey( formDisplay.getCompositeId( ) );
    		
    		if ( _question == null )
            {
                return redirectToViewManageForm( request );
            }
    		
    		Object [ ] args = {
    				_question.getTitle( ),
                };
            
    		_strControlTitle = I18nService.getLocalizedString( INFO_CONDITION_QUESTION_TITLE, args, request.getLocale( ) );
    	}
    	else if( formDisplay.getCompositeType( ).equals( FormsConstants.COMPOSITE_GROUP_TYPE ) )
    	{
    		_group = GroupHome.findByPrimaryKey( formDisplay.getCompositeId( ) );
    		
    		if ( _group == null )
            {
                return redirectToViewManageForm( request );
            }
    		
    		Object [ ] args = {
    				_group.getTitle( ),
                };
            
    		_strControlTitle = I18nService.getLocalizedString( INFO_CONDITION_GROUP_TITLE, args, request.getLocale( ) );
    	}
    	        
        _control = ControlHome.getConditionalDisplayControlByDisplay( nIdCompositeDisplay );

        if ( _control == null )
        {
            _control = new Control( );
            _control.setIdTargetFormDisplay( nIdCompositeDisplay );
            _control.setControlType( ControlType.CONDITIONAL.getLabel( ) );
        }
        
        _strControlTemplate = TEMPLATE_MODIFY_CONDITION_CONTROL;
        
        return redirectView( request, VIEW_MODIFY_CONTROL );
    }

    /**
     * Returns the form to modify a control
     *
     * @param request
     *            The Http request
     * @return the html code of the control form
     */
    @View( VIEW_MODIFY_CONTROL )
    public String getModifyControl( HttpServletRequest request )
    {
        if ( _control == null && !retrieveControlFromRequest( request ) )
        {
            return redirectToViewManageForm( request );
        }

        Map<String, Object> model = getModel( );

        if ( _control != null )
        {
            buildControlModel( request, model );
        }
        else
        {
            redirectToViewManageForm( request );
        }

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_CONTROL, _strControlTemplate, model );
    }

    /**
     * Build the model for Create and Modify Control views
     * 
     * @param request
     *            the Http request
     * @param model
     *            the Model
     */
    private void buildControlModel( HttpServletRequest request, Map<String, Object> model )
    {
    	String strValidatorName = request.getParameter( FormsConstants.PARAMETER_VALIDATOR_NAME );
        
        if( StringUtils.isNotEmpty( strValidatorName ) && _control.getValidatorName( ) != strValidatorName )
        {
        	_control.setValidatorName( strValidatorName );
        	_control.setValue( StringUtils.EMPTY );
        }

        int nIdQuestion = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_QUESTION ), FormsConstants.DEFAULT_ID_VALUE );

        if ( nIdQuestion != FormsConstants.DEFAULT_ID_VALUE && _control.getIdQuestion( ) != nIdQuestion )
        {
            _control.setIdQuestion( nIdQuestion );
            _control.setValidatorName( StringUtils.EMPTY );
            _control.setValue( StringUtils.EMPTY );
        }
            	
        if ( _control.getIdQuestion( ) != FormsConstants.DEFAULT_ID_VALUE )
        {
            Question question = QuestionHome.findByPrimaryKey( _control.getIdQuestion( ) );

            if ( question != null && question.getEntry( ) != null )
            {
                EntryType entryType = question.getEntry( ).getEntryType( );
                ReferenceList refListAvailableValidator = EntryServiceManager.getInstance( ).getRefListAvailableValidator( entryType );

                model.put( FormsConstants.MARK_AVAILABLE_VALIDATORS, refListAvailableValidator );
                
                if( refListAvailableValidator.size( ) >= 1 )
                {
                	_control.setValidatorName( refListAvailableValidator.get( 0 ).getCode( ) );
                }
            }
        }
        
        String strValidatorTemplate = StringUtils.EMPTY;
        
        if( StringUtils.isNotEmpty( _control.getValidatorName( ) ) )
        {
        	IValidator validator = EntryServiceManager.getInstance( ).getValidator( _control.getValidatorName( ) );
        	strValidatorTemplate = validator.getDisplayHtml( _control );
        }

        model.put( FormsConstants.MARK_TRANSITION, _transition );
        model.put( FormsConstants.MARK_QUESTION , _question );
        model.put( FormsConstants.MARK_STEP, _step );
        model.put( FormsConstants.MARK_CONTROL_TEMPLATE, strValidatorTemplate );
        model.put( FormsConstants.MARK_CONTROL, _control );
        model.put( FormsConstants.MARK_QUESTION_LIST, QuestionHome.getQuestionsReferenceListByForm( _step.getIdForm( ) ) );
        model.put( FormsConstants.MARK_CONDITION_TITLE , _strControlTitle );
    }

    /**
     * Process the data modification of a control
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_CONTROL )
    public String doModifyControl( HttpServletRequest request )
    {
        if ( !populateAndValidateControl( request ) )
        {
            return redirectView( request, VIEW_MODIFY_CONTROL );
        }

        if( _control.getId( ) > 0 )
        {
        	ControlHome.update( _control );
        	request.setAttribute( FormsConstants.PARAMETER_INFO_KEY, INFO_CONTROL_UPDATED );
        }
        else
        {
        	ControlHome.create( _control );
        	
        	if( _transition != null )
        	{
        		_transition.setIdControl( _control.getId( ) );
        		TransitionHome.update( _transition );
        	}
        	
        	request.setAttribute( FormsConstants.PARAMETER_INFO_KEY, INFO_CONTROL_CREATED );
        }

        return redirect( request, getControlReturnUrl( request ) );
    }

    /**
     * Manages the removal form of a Control whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @View( VIEW_CONFIRM_REMOVE_CONTROL )
    public String getConfirmRemoveControl( HttpServletRequest request )
    {
        int nIdControlToRemove = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_CONTROL ), FormsConstants.DEFAULT_ID_VALUE );

        if ( nIdControlToRemove == FormsConstants.DEFAULT_ID_VALUE )
        {
        	int nIdTransitionControlToRemove = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_TRANSITION ), FormsConstants.DEFAULT_ID_VALUE );
        	_transition = TransitionHome.findByPrimaryKey( nIdTransitionControlToRemove );
        	
        	if( nIdTransitionControlToRemove == FormsConstants.DEFAULT_ID_VALUE )
        	{
        		return redirectToViewManageForm( request );
        	}
        	
        	nIdControlToRemove = _transition.getIdControl( );
        }

        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_CONTROL ) );
        url.addParameter( FormsConstants.PARAMETER_ID_CONTROL, nIdControlToRemove );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_CONTROL, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );

    }

    /**
     * Handles the removal of a Transition
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage Transition
     */
    @Action( ACTION_REMOVE_CONTROL )
    public String doRemoveControl( HttpServletRequest request )
    {
        if ( !retrieveControlFromRequest( request ) )
        {
            return redirectToViewManageForm( request );
        }

        int nIdControlToRemove = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_CONTROL ), FormsConstants.DEFAULT_ID_VALUE );

        _control = ControlHome.findByPrimaryKey( nIdControlToRemove );
        
        if ( _control != null )
        {
            ControlHome.remove( _control.getId( ) );
                        
            if ( _transition != null )
            {
            	_transition.setIdControl( 0 );
                TransitionHome.update( _transition );
            }
            
            request.setAttribute( FormsConstants.PARAMETER_INFO_KEY, INFO_CONTROL_REMOVED );
        }
        else
        {
            addError( ERROR_CONTROL_REMOVED, getLocale( ) );
            return redirectToViewManageForm( request );
        }
        
        return redirect( request, getControlReturnUrl( request ) );
    }
    
    /**
     * Manages the removal form of a Control whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CANCEL_AND_RETURN )
    public String getReturnUrl( HttpServletRequest request )
    {
        return redirect( request, getControlReturnUrl( request ) );
    }

    
    /**
     * Retrieve the control object from request parameter
     * 
     * @param request
     *            The request
     * 
     * @return false if an error occurred, true otherwise
     */
    private boolean retrieveControlFromRequest( HttpServletRequest request )
    {
        boolean bSuccess = true;
        int nIdControl = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_CONTROL ), FormsConstants.DEFAULT_ID_VALUE );

        if ( nIdControl == FormsConstants.DEFAULT_ID_VALUE )
        {
            bSuccess = false;
        }

        if ( _control == null || _control.getId( ) != nIdControl )
        {
        	_control = ControlHome.findByPrimaryKey( nIdControl );
        }
        return bSuccess;
    }

    /**
     * Validate the Control field values
     * @param request
     *            the Http request
     * @return True if the control is valid
     */
    private boolean populateAndValidateControl( HttpServletRequest request )
    {
    	int nIdQuestion = NumberUtils.toInt( request.getParameter( FormsConstants.PARAMETER_ID_QUESTION ), FormsConstants.DEFAULT_ID_VALUE );
    	
    	if( nIdQuestion > 0 && StringUtils.isNotEmpty( _control.getValidatorName( ) ) )
    	{
    		_control.setIdQuestion( nIdQuestion );
    		Question question = QuestionHome.findByPrimaryKey( _control.getIdQuestion( ) );
    		if( question.getEntry( ) != null )
    		{
    			List<IValidator> listValidator = EntryServiceManager.getInstance( ).getListAvailableValidator( question.getEntry( ).getEntryType( ) );
    			IValidator controlValidator = EntryServiceManager.getInstance( ).getValidator( _control.getValidatorName( ) );
    			
    			if( !listValidator.contains( controlValidator ) )
    			{
    				_control.setValidatorName( StringUtils.EMPTY );
    				_control.setValue( StringUtils.EMPTY );
    				addError( ERROR_QUESTION_VALIDATOR_MATCH, getLocale( ) );
    				return false;
    			}
    		}
    	}
    	
    	String strValidatorName = request.getParameter( FormsConstants.PARAMETER_VALIDATOR_NAME );
        
        if( StringUtils.isNotEmpty( _control.getValidatorName( ) ) && !_control.getValidatorName( ).equals( strValidatorName ) )
        {
        	addError( ERROR_VALIDATOR_VALUE_MATCH, getLocale( ) );
        	_control.setValue( StringUtils.EMPTY );
        	return false;
        }
        
        populate( _control, request, getLocale( ) );
        return validateBean( _control, CONTROL_VALIDATION_ATTRIBUTES_PREFIX );
    }

    /**
     * 
     * @param request
     * 			The Http request
     * @return the return URL string
     */
    private String getControlReturnUrl( HttpServletRequest request )
    {    
    	String strTargetJsp = StringUtils.EMPTY;
    	int nIdStep;
    	
    	if( _transition != null )
    	{
    		strTargetJsp = FormsConstants.JSP_MANAGE_TRANSITIONS;
    		nIdStep = _transition.getFromStep( );
    	}
    	else
    	{
    		strTargetJsp = FormsConstants.JSP_MANAGE_QUESTIONS;
    		if( _question != null )
    		{
    			nIdStep = _question.getIdStep( );
    		}
    		else
    		{
    			nIdStep = _group.getIdStep( );
    		}
    	}
    	
    	_strControlTemplate = null;
    	_step = null;
    	_control = null;
    	_transition = null;
    	_question = null;
    	_group = null;
    	_strControlTitle = null;
        
    	UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + strTargetJsp );
		url.addParameter( FormsConstants.PARAMETER_ID_STEP, nIdStep );
		
		String strInfoKey = (String) request.getAttribute( FormsConstants.PARAMETER_INFO_KEY );
        if( StringUtils.isNotEmpty( strInfoKey ) )
        {
        	url.addParameter( FormsConstants.PARAMETER_INFO_KEY, strInfoKey );
        }
		
    	return url.getUrl( );
    }
}